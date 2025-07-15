package com.bankingapp.repository;

import com.bankingapp.entity.Transaction;
import com.bankingapp.entity.Account;
import com.bankingapp.dto.TransactionSummaryDTO;
import com.bankingapp.dto.TransactionStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations
 * Updated with corrected method signatures and optimized queries
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // ===================== Basic Transaction Queries =====================

    /**
     * Find transaction by transaction ID
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     * Check if transaction ID exists
     */
    Boolean existsByTransactionId(String transactionId);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    /**
     * Find transactions by type
     */
    List<Transaction> findByTransactionType(Transaction.TransactionType transactionType);

    /**
     * Find transactions by type and status
     */
    List<Transaction> findByTransactionTypeAndStatus(Transaction.TransactionType transactionType, 
                                                   Transaction.TransactionStatus status);

    // ===================== Account-Based Queries =====================

    /**
     * Find all transactions for a specific account (both from and to)
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find transactions for account with pagination
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Find transactions from a specific account
     */
    List<Transaction> findByFromAccountIdOrderByCreatedAtDesc(Long fromAccountId);

    /**
     * Find transactions to a specific account
     */
    List<Transaction> findByToAccountIdOrderByCreatedAtDesc(Long toAccountId);

    /**
     * Find account transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findAccountTransactionsByDateRange(@Param("accountId") Long accountId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent transactions for account
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.createdAt >= :sinceDate ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByAccount(@Param("accountId") Long accountId,
                                                    @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find latest N transactions for account
     */
    @Query(value = "SELECT * FROM transactions t WHERE (t.from_account_id = :accountId OR t.to_account_id = :accountId) " +
           "ORDER BY t.created_at DESC LIMIT :limit", nativeQuery = true)
    List<Transaction> findLatestTransactionsByAccount(@Param("accountId") Long accountId, @Param("limit") int limit);

    // ===================== User-Based Queries =====================

    /**
     * Find all transactions for user accounts
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    /**
     * Find user transactions with pagination
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find user transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findUserTransactionsByDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // ===================== Transaction Counting Queries =====================

    /**
     * Count transactions for account
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId")
    Long countByAccountId(@Param("accountId") Long accountId);

    /**
     * Count completed transactions for account
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.status = 'COMPLETED'")
    Long countCompletedTransactionsByAccount(@Param("accountId") Long accountId);

    /**
     * Count pending transactions for account
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.status = 'PENDING'")
    Long countPendingTransactionsByAccount(@Param("accountId") Long accountId);

    /**
     * Count failed transactions for account
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.status = 'FAILED'")
    Long countFailedTransactionsByAccount(@Param("accountId") Long accountId);

    // ===================== Sum and Aggregation Queries =====================

    /**
     * Sum deposits for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccount.id = :accountId " +
           "AND t.transactionType = 'DEPOSIT' AND t.status = 'COMPLETED'")
    BigDecimal sumDepositsByAccount(@Param("accountId") Long accountId);

    /**
     * Sum withdrawals for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionType = 'WITHDRAWAL' AND t.status = 'COMPLETED'")
    BigDecimal sumWithdrawalsByAccount(@Param("accountId") Long accountId);

    /**
     * Sum transfers out for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionType = 'TRANSFER' AND t.status = 'COMPLETED'")
    BigDecimal sumTransfersOutByAccount(@Param("accountId") Long accountId);

    /**
     * Sum transfers in for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccount.id = :accountId " +
           "AND t.transactionType = 'TRANSFER' AND t.status = 'COMPLETED'")
    BigDecimal sumTransfersInByAccount(@Param("accountId") Long accountId);

    /**
     * Get monthly spending for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND (t.transactionType = 'WITHDRAWAL' OR t.transactionType = 'TRANSFER') " +
           "AND t.createdAt >= :monthStart AND t.status = 'COMPLETED'")
    BigDecimal getMonthlySpending(@Param("accountId") Long accountId, @Param("monthStart") LocalDateTime monthStart);

    /**
     * Get monthly income for account
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccount.id = :accountId " +
           "AND (t.transactionType = 'DEPOSIT' OR t.transactionType = 'TRANSFER') " +
           "AND t.createdAt >= :monthStart AND t.status = 'COMPLETED'")
    BigDecimal getMonthlyIncome(@Param("accountId") Long accountId, @Param("monthStart") LocalDateTime monthStart);

    // ===================== Search and Filter Queries =====================

    /**
     * Search transactions by multiple criteria
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:accountId IS NULL OR t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND " +
           "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
           "(:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Transaction> searchTransactions(@Param("accountId") Long accountId,
                                       @Param("transactionType") Transaction.TransactionType transactionType,
                                       @Param("status") Transaction.TransactionStatus status,
                                       @Param("minAmount") BigDecimal minAmount,
                                       @Param("maxAmount") BigDecimal maxAmount,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("description") String description,
                                       Pageable pageable);

    /**
     * Get transaction history with filters
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> getTransactionHistory(@Param("accountId") Long accountId,
                                          @Param("transactionType") Transaction.TransactionType transactionType,
                                          @Param("status") Transaction.TransactionStatus status,
                                          Pageable pageable);

    // ===================== Status Management Queries =====================

    /**
     * Update transaction status
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = :status WHERE t.id = :transactionId")
    int updateTransactionStatus(@Param("transactionId") Long transactionId, @Param("status") Transaction.TransactionStatus status);

    /**
     * Update transaction status by transaction ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = :status WHERE t.transactionId = :transactionId")
    int updateTransactionStatusByTransactionId(@Param("transactionId") String transactionId, @Param("status") Transaction.TransactionStatus status);

    /**
     * Mark transactions as failed after timeout
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = 'FAILED' WHERE t.status = 'PENDING' AND t.createdAt < :timeoutDate")
    int markTimeoutTransactionsAsFailed(@Param("timeoutDate") LocalDateTime timeoutDate);

    // ===================== Validation and Utility Queries =====================

    /**
     * Check if transaction belongs to user's account
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.transactionId = :transactionId AND " +
           "(t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId)")
    Boolean isTransactionOwnedByUser(@Param("transactionId") String transactionId, @Param("userId") Long userId);

    /**
     * Check if account has any transactions
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId")
    Boolean hasTransactions(@Param("accountId") Long accountId);

    /**
     * Get last transaction date for account
     */
    @Query("SELECT MAX(t.createdAt) FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId")
    LocalDateTime getLastTransactionDate(@Param("accountId") Long accountId);

    // ===================== Statistics and Analytics =====================

    /**
     * Get transaction count by type for account
     */
    @Query("SELECT t.transactionType, COUNT(t) FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.status = 'COMPLETED' " +
           "GROUP BY t.transactionType")
    List<Object[]> getTransactionCountByTypeForAccount(@Param("accountId") Long accountId);

    /**
     * Get daily transaction stats for account
     */
    @Query("SELECT DATE(t.createdAt), COUNT(t), SUM(t.amount) FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND t.createdAt >= :startDate " +
           "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
    List<Object[]> getDailyTransactionStatsForAccount(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate);

    /**
     * Get account transaction summaries
     */
    @Query("SELECT new com.bank.dto.TransactionSummaryDTO(" +
           "t.id, t.transactionId, t.transactionType, t.amount, t.status, t.createdAt, t.description) " +
           "FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionSummaryDTO> getTransactionSummariesByAccount(@Param("accountId") Long accountId, Pageable pageable);

    // ===================== Security and Monitoring =====================

    /**
     * Find large transactions above threshold
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold AND t.status = 'COMPLETED' " +
           "ORDER BY t.amount DESC")
    List<Transaction> findLargeTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Find suspicious rapid transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId AND t.createdAt >= :sinceTime " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findRapidTransactions(@Param("accountId") Long accountId, @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Find failed transactions for analysis
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.createdAt >= :startDate " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findFailedTransactionsForAnalysis(@Param("startDate") LocalDateTime startDate);

    // ===================== Advanced Analytics =====================

    /**
     * Get cash flow analysis for account
     */
    @Query("SELECT " +
           "SUM(CASE WHEN t.toAccount.id = :accountId AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END) as totalInflow, " +
           "SUM(CASE WHEN t.fromAccount.id = :accountId AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END) as totalOutflow, " +
           "COUNT(CASE WHEN t.toAccount.id = :accountId THEN 1 END) as inflowCount, " +
           "COUNT(CASE WHEN t.fromAccount.id = :accountId THEN 1 END) as outflowCount " +
           "FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Object[]> getCashFlowAnalysis(@Param("accountId") Long accountId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions between two specific accounts
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "((t.fromAccount.id = :account1Id AND t.toAccount.id = :account2Id) OR " +
           "(t.fromAccount.id = :account2Id AND t.toAccount.id = :account1Id)) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsBetweenAccounts(@Param("account1Id") Long account1Id, 
                                                     @Param("account2Id") Long account2Id);
}