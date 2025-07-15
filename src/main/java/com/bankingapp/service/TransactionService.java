package com.bankingapp.service;

import com.bankingapp.dto.*;
import com.bankingapp.entity.Account;
import com.bankingapp.entity.Transaction;
import com.bankingapp.exception.AccountNotFoundException;
import com.bankingapp.exception.InsufficientFundsException;
import com.bankingapp.exception.TransactionNotFoundException;
import com.bankingapp.repository.AccountRepository;
import com.bankingapp.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service class for Transaction management operations
 * Handles all transaction-related business logic with proper error handling and audit logging
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Value("${app.kafka.topics.transaction}")
    private String transactionTopic;

    @Value("${app.transaction.timeout.minutes:30}")
    private int transactionTimeoutMinutes;

    // ===================== Core Transaction Operations =====================

    /**
     * Process deposit transaction
     */
    public TransactionDTO deposit(TransactionRequest request) {
        logger.info("Processing deposit for account: {}, amount: {}", request.getAccountId(), request.getAmount());

        Account account = getAccountById(request.getAccountId());
        validateAccountForDeposit(account);

        Transaction transaction = createTransaction(
            null, account, Transaction.TransactionType.DEPOSIT, 
            request.getAmount(), request.getDescription()
        );

        try {
            // Update account balance
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            // Mark transaction as completed
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Send notifications and events
            sendTransactionEvent(savedTransaction, "DEPOSIT_COMPLETED");
            if (request.getSendNotification()) {
                notificationService.sendDepositNotification(account.getUser(), savedTransaction);
            }

            // Audit logging
            auditService.logTransaction(savedTransaction, "DEPOSIT_COMPLETED");

            logger.info("Deposit completed successfully for transaction: {}", transaction.getTransactionId());
            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            handleTransactionFailure(transaction, e);
            throw new RuntimeException("Deposit transaction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Process withdrawal transaction
     */
    public TransactionDTO withdraw(TransactionRequest request) {
        logger.info("Processing withdrawal for account: {}, amount: {}", request.getAccountId(), request.getAmount());

        Account account = getAccountById(request.getAccountId());
        validateAccountForWithdrawal(account, request.getAmount());

        Transaction transaction = createTransaction(
            account, null, Transaction.TransactionType.WITHDRAWAL, 
            request.getAmount(), request.getDescription()
        );

        try {
            // Update account balance
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);

            // Mark transaction as completed
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Send notifications and events
            sendTransactionEvent(savedTransaction, "WITHDRAWAL_COMPLETED");
            if (request.getSendNotification()) {
                notificationService.sendWithdrawalNotification(account.getUser(), savedTransaction);
            }

            // Audit logging
            auditService.logTransaction(savedTransaction, "WITHDRAWAL_COMPLETED");

            logger.info("Withdrawal completed successfully for transaction: {}", transaction.getTransactionId());
            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            handleTransactionFailure(transaction, e);
            throw new RuntimeException("Withdrawal transaction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Process transfer transaction
     */
    public TransactionDTO transfer(TransferRequest request) {
        logger.info("Processing transfer from account: {} to account: {}, amount: {}", 
                   request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // Validate transfer request
        validateTransferRequest(request);

        Account fromAccount = getAccountById(request.getFromAccountId());
        Account toAccount = getAccountById(request.getToAccountId());
        
        validateAccountForWithdrawal(fromAccount, request.getAmount());
        validateAccountForDeposit(toAccount);

        Transaction transaction = createTransaction(
            fromAccount, toAccount, Transaction.TransactionType.TRANSFER, 
            request.getAmount(), request.getDescription()
        );

        try {
            // Update account balances atomically
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Mark transaction as completed
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Send notifications and events
            sendTransactionEvent(savedTransaction, "TRANSFER_COMPLETED");
            if (request.getSendNotification()) {
                notificationService.sendTransferNotification(fromAccount.getUser(), toAccount.getUser(), savedTransaction);
            }

            // Audit logging
            auditService.logTransaction(savedTransaction, "TRANSFER_COMPLETED");

            logger.info("Transfer completed successfully for transaction: {}", transaction.getTransactionId());
            return convertToDTO(savedTransaction);

        } catch (Exception e) {
            handleTransactionFailure(transaction, e);
            throw new RuntimeException("Transfer transaction failed: " + e.getMessage(), e);
        }
    }

    // ===================== Transaction Retrieval Operations =====================

    /**
     * Get transaction by ID
     */
    public TransactionDTO getTransactionById(Long transactionId) {
        logger.debug("Fetching transaction by ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));
        
        return convertToDTO(transaction);
    }

    /**
     * Get transaction by transaction ID
     */
    public TransactionDTO getTransactionByTransactionId(String transactionId) {
        logger.debug("Fetching transaction by transaction ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));
        
        return convertToDTO(transaction);
    }

    /**
     * Get transaction history for account
     */
    public Page<TransactionDTO> getTransactionHistory(Long accountId, int page, int size, String sortBy, String sortDirection) {
        logger.debug("Fetching transaction history for account: {}", accountId);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        
        return transactions.map(this::convertToDTO);
    }

    /**
     * Get filtered transaction history
     */
    public Page<TransactionDTO> getFilteredTransactionHistory(TransactionFilterDTO filter) {
        logger.debug("Fetching filtered transaction history: {}", filter);
        
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        Page<Transaction> transactions = transactionRepository.getTransactionHistory(
            filter.getAccountId(), filter.getTransactionType(), filter.getStatus(), pageable);
        
        return transactions.map(this::convertToDTO);
    }

    /**
     * Search transactions with advanced criteria
     */
    public Page<TransactionDTO> searchTransactions(TransactionFilterDTO filter) {
        logger.debug("Searching transactions with criteria: {}", filter);
        
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        Page<Transaction> transactions = transactionRepository.searchTransactions(
            filter.getAccountId(), filter.getTransactionType(), filter.getStatus(),
            filter.getMinAmount(), filter.getMaxAmount(), filter.getStartDate(),
            filter.getEndDate(), filter.getDescription(), pageable
        );
        
        return transactions.map(this::convertToDTO);
    }

    /**
     * Get recent transactions for account
     */
    public List<TransactionDTO> getRecentTransactions(Long accountId, int days) {
        logger.debug("Fetching recent transactions for account: {} (last {} days)", accountId, days);
        
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        List<Transaction> transactions = transactionRepository.findRecentTransactionsByAccount(accountId, sinceDate);
        
        return transactions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get latest transactions for account
     */
    public List<TransactionDTO> getLatestTransactions(Long accountId, int limit) {
        logger.debug("Fetching latest {} transactions for account: {}", limit, accountId);
        
        List<Transaction> transactions = transactionRepository.findLatestTransactionsByAccount(accountId, limit);
        
        return transactions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // ===================== Transaction Statistics =====================

    /**
     * Get transaction count for account
     */
    public Long getTransactionCount(Long accountId) {
        logger.debug("Getting transaction count for account: {}", accountId);
        return transactionRepository.countByAccountId(accountId);
    }

    /**
     * Get monthly spending for account
     */
    public BigDecimal getMonthlySpending(Long accountId, LocalDateTime monthStart) {
        logger.debug("Getting monthly spending for account: {}", accountId);
        return transactionRepository.getMonthlySpending(accountId, monthStart);
    }

    /**
     * Get monthly income for account
     */
    public BigDecimal getMonthlyIncome(Long accountId, LocalDateTime monthStart) {
        logger.debug("Getting monthly income for account: {}", accountId);
        return transactionRepository.getMonthlyIncome(accountId, monthStart);
    }

    /**
     * Get transaction statistics for account
     */
    public TransactionStatisticsDTO getTransactionStatistics(Long accountId, String period) {
        logger.debug("Getting transaction statistics for account: {} (period: {})", accountId, period);
        
        LocalDateTime startDate = calculatePeriodStartDate(period);
        
        // Get basic statistics
        Long totalCount = transactionRepository.countByAccountId(accountId);
        Long completedCount = transactionRepository.countCompletedTransactionsByAccount(accountId);
        Long pendingCount = transactionRepository.countPendingTransactionsByAccount(accountId);
        Long failedCount = transactionRepository.countFailedTransactionsByAccount(accountId);
        
        // Get sum statistics
        BigDecimal totalDeposits = transactionRepository.sumDepositsByAccount(accountId);
        BigDecimal totalWithdrawals = transactionRepository.sumWithdrawalsByAccount(accountId);
        BigDecimal totalTransfersOut = transactionRepository.sumTransfersOutByAccount(accountId);
        BigDecimal total