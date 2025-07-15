package com.bankingapp.repository;

import com.bankingapp.entity.Account;
import com.bankingapp.dto.AccountSummaryDTO;
import com.bankingapp.dto.AccountStatisticsDTO;
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
 * Repository interface for Account entity operations
 * Provides custom queries for banking operations
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    // ===================== Basic CRUD Operations =====================

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if account number exists
     */
    Boolean existsByAccountNumber(String accountNumber);

    /**
     * Find account by account number and user ID for security
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber AND a.user.id = :userId")
    Optional<Account> findByAccountNumberAndUserId(@Param("accountNumber") String accountNumber, 
                                                  @Param("userId") Long userId);

    // ===================== User Account Queries =====================

    /**
     * Find all accounts for a specific user
     */
    List<Account> findByUserId(Long userId);

    /**
     * Find all active accounts for a specific user
     */
    List<Account> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all inactive accounts for a specific user
     */
    List<Account> findByUserIdAndIsActiveFalse(Long userId);

    /**
     * Find accounts by user ID and account type
     */
    List<Account> findByUserIdAndAccountType(Long userId, Account.AccountType accountType);

    /**
     * Find active accounts by user ID and account type
     */
    List<Account> findByUserIdAndAccountTypeAndIsActiveTrue(Long userId, Account.AccountType accountType);

    /**
     * Count total accounts for a user
     */
    Long countByUserId(Long userId);

    /**
     * Count active accounts for a user
     */
    Long countByUserIdAndIsActiveTrue(Long userId);

    // ===================== Account Type Queries =====================

    /**
     * Find all accounts by type
     */
    List<Account> findByAccountType(Account.AccountType accountType);

    /**
     * Find all active accounts by type
     */
    List<Account> findByAccountTypeAndIsActiveTrue(Account.AccountType accountType);

    /**
     * Count accounts by type
     */
    Long countByAccountType(Account.AccountType accountType);

    /**
     * Count active accounts by type
     */
    Long countByAccountTypeAndIsActiveTrue(Account.AccountType accountType);

    // ===================== Balance Queries =====================

    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance AND a.isActive = true")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    /**
     * Find accounts with balance less than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance < :maxBalance AND a.isActive = true")
    List<Account> findAccountsWithBalanceLessThan(@Param("maxBalance") BigDecimal maxBalance);

    /**
     * Find accounts with balance between specified amounts
     */
    @Query("SELECT a FROM Account a WHERE a.balance BETWEEN :minBalance AND :maxBalance AND a.isActive = true")
    List<Account> findAccountsWithBalanceBetween(@Param("minBalance") BigDecimal minBalance, 
                                               @Param("maxBalance") BigDecimal maxBalance);

    /**
     * Find accounts with zero balance
     */
    @Query("SELECT a FROM Account a WHERE a.balance = 0 AND a.isActive = true")
    List<Account> findAccountsWithZeroBalance();

    /**
     * Find accounts with negative balance
     */
    @Query("SELECT a FROM Account a WHERE a.balance < 0 AND a.isActive = true")
    List<Account> findAccountsWithNegativeBalance();

    /**
     * Get total balance for all user accounts
     */
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    /**
     * Get total balance by account type for a user
     */
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType AND a.isActive = true")
    BigDecimal getTotalBalanceByUserIdAndAccountType(@Param("userId") Long userId, 
                                                   @Param("accountType") Account.AccountType accountType);

    // ===================== Account Summary Queries =====================

    /**
     * Get account summaries for a user
     */
    @Query("SELECT new com.bank.dto.AccountSummaryDTO(" +
           "a.id, a.accountNumber, a.accountType, a.balance, a.isActive) " +
           "FROM Account a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<AccountSummaryDTO> getAccountSummariesByUserId(@Param("userId") Long userId);

    /**
     * Get active account summaries for a user
     */
    @Query("SELECT new com.bank.dto.AccountSummaryDTO(" +
           "a.id, a.accountNumber, a.accountType, a.balance, a.isActive) " +
           "FROM Account a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.createdAt DESC")
    List<AccountSummaryDTO> getActiveAccountSummariesByUserId(@Param("userId") Long userId);

    // ===================== Search and Filter Queries =====================

    /**
     * Search accounts by multiple criteria
     */
    @Query("SELECT a FROM Account a WHERE " +
           "(:accountNumber IS NULL OR a.accountNumber LIKE %:accountNumber%) AND " +
           "(:accountType IS NULL OR a.accountType = :accountType) AND " +
           "(:username IS NULL OR LOWER(a.user.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<Account> searchAccounts(@Param("accountNumber") String accountNumber,
                                @Param("accountType") Account.AccountType accountType,
                                @Param("username") String username,
                                @Param("isActive") Boolean isActive,
                                Pageable pageable);

    /**
     * Find accounts created within date range
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Account> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find recently created accounts
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt >= :sinceDate ORDER BY a.createdAt DESC")
    List<Account> findRecentlyCreatedAccounts(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find dormant accounts (no transactions for specified period)
     */
    @Query("SELECT a FROM Account a WHERE a.id NOT IN " +
           "(SELECT DISTINCT t.fromAccount.id FROM Transaction t WHERE t.createdAt >= :sinceDate " +
           "UNION SELECT DISTINCT t.toAccount.id FROM Transaction t WHERE t.createdAt >= :sinceDate AND t.toAccount IS NOT NULL) " +
           "AND a.isActive = true")
    List<Account> findDormantAccounts(@Param("sinceDate") LocalDateTime sinceDate);

    // ===================== Statistics and Analytics =====================

    /**
     * Get account statistics by user
     */
    @Query("SELECT new com.bank.dto.AccountStatisticsDTO(" +
           "COUNT(a), " +
           "SUM(CASE WHEN a.isActive = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.isActive = false THEN 1 ELSE 0 END), " +
           "AVG(a.balance), " +
           "SUM(a.balance), " +
           "MAX(a.balance), " +
           "MIN(a.balance)) " +
           "FROM Account a WHERE a.user.id = :userId")
    AccountStatisticsDTO getAccountStatisticsByUserId(@Param("userId") Long userId);

    /**
     * Get global account statistics
     */
    @Query("SELECT new com.bank.dto.AccountStatisticsDTO(" +
           "COUNT(a), " +
           "SUM(CASE WHEN a.isActive = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.isActive = false THEN 1 ELSE 0 END), " +
           "AVG(a.balance), " +
           "SUM(a.balance), " +
           "MAX(a.balance), " +
           "MIN(a.balance)) " +
           "FROM Account a")
    AccountStatisticsDTO getGlobalAccountStatistics();

    /**
     * Get account count by type
     */
    @Query("SELECT a.accountType, COUNT(a) FROM Account a WHERE a.isActive = true GROUP BY a.accountType")
    List<Object[]> getAccountCountByType();

    /**
     * Get balance distribution by account type
     */
    @Query("SELECT a.accountType, SUM(a.balance), AVG(a.balance), COUNT(a) " +
           "FROM Account a WHERE a.isActive = true GROUP BY a.accountType")
    List<Object[]> getBalanceDistributionByType();

    /**
     * Get monthly account creation statistics
     */
    @Query("SELECT DATE_FORMAT(a.createdAt, '%Y-%m'), COUNT(a) " +
           "FROM Account a WHERE a.createdAt >= :startDate " +
           "GROUP BY DATE_FORMAT(a.createdAt, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(a.createdAt, '%Y-%m')")
    List<Object[]> getMonthlyAccountCreationStats(@Param("startDate") LocalDateTime startDate);

    // ===================== Balance Management Queries =====================

    /**
     * Update account balance
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    int updateAccountBalance(@Param("accountId") Long accountId, @Param("newBalance") BigDecimal newBalance);

    /**
     * Add amount to account balance
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = a.balance + :amount, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    int addToAccountBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * Subtract amount from account balance
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = a.balance - :amount, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    int subtractFromAccountBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * Update account balance if current balance matches expected
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :newBalance, a.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE a.id = :accountId AND a.balance = :expectedBalance")
    int updateAccountBalanceIfMatches(@Param("accountId") Long accountId, 
                                    @Param("newBalance") BigDecimal newBalance,
                                    @Param("expectedBalance") BigDecimal expectedBalance);

    // ===================== Account Status Management =====================

    /**
     * Activate account
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.isActive = true, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    int activateAccount(@Param("accountId") Long accountId);

    /**
     * Deactivate account
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.isActive = false, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :accountId")
    int deactivateAccount(@Param("accountId") Long accountId);

    /**
     * Deactivate all user accounts
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.isActive = false, a.updatedAt = CURRENT_TIMESTAMP WHERE a.user.id = :userId")
    int deactivateAllUserAccounts(@Param("userId") Long userId);

    // ===================== Account Validation Queries =====================

    /**
     * Check if account belongs to user
     */
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.id = :accountId AND a.user.id = :userId")
    Boolean isAccountOwnedByUser(@Param("accountId") Long accountId, @Param("userId") Long userId);

    /**
     * Check if account is active
     */
    @Query("SELECT a.isActive FROM Account a WHERE a.id = :accountId")
    Boolean isAccountActive(@Param("accountId") Long accountId);

    /**
     * Check if account has sufficient balance
     */
    @Query("SELECT a.balance >= :amount FROM Account a WHERE a.id = :accountId")
    Boolean hasSufficientBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * Get account balance for validation
     */
    @Query("SELECT a.balance FROM Account a WHERE a.id = :accountId")
    BigDecimal getAccountBalance(@Param("accountId") Long accountId);

    // ===================== Reporting Queries =====================

    /**
     * Get accounts with high balances
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :threshold AND a.isActive = true ORDER BY a.balance DESC")
    List<Account> getHighBalanceAccounts(@Param("threshold") BigDecimal threshold);

    /**
     * Get top accounts by balance
     */
    @Query("SELECT a FROM Account a WHERE a.isActive = true ORDER BY a.balance DESC")
    Page<Account> getTopAccountsByBalance(Pageable pageable);

    /**
     * Get accounts by balance range with pagination
     */
    @Query("SELECT a FROM Account a WHERE a.balance BETWEEN :minBalance AND :maxBalance AND a.isActive = true")
    Page<Account> getAccountsByBalanceRange(@Param("minBalance") BigDecimal minBalance,
                                          @Param("maxBalance") BigDecimal maxBalance,
                                          Pageable pageable);

    /**
     * Get user accounts ordered by creation date
     */
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<Account> getUserAccountsOrderedByCreation(@Param("userId") Long userId);

    /**
     * Get user accounts ordered by balance
     */
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.balance DESC")
    List<Account> getUserAccountsOrderedByBalance(@Param("userId") Long userId);

    // ===================== Admin and Monitoring Queries =====================

    /**
     * Find accounts that need attention (negative balance, etc.)
     */
    @Query("SELECT a FROM Account a WHERE (a.balance < 0 OR a.balance > :highBalanceThreshold) AND a.isActive = true")
    List<Account> getAccountsNeedingAttention(@Param("highBalanceThreshold") BigDecimal highBalanceThreshold);

    /**
     * Get accounts with unusual activity patterns
     */
    @Query("SELECT a FROM Account a WHERE a.id IN " +
           "(SELECT t.fromAccount.id FROM Transaction t WHERE t.createdAt >= :sinceDate " +
           "GROUP BY t.fromAccount.id HAVING COUNT(t) > :transactionThreshold)")
    List<Account> getAccountsWithHighActivity(@Param("sinceDate") LocalDateTime sinceDate,
                                            @Param("transactionThreshold") Long transactionThreshold);

    /**
     * Get recently modified accounts
     */
    @Query("SELECT a FROM Account a WHERE a.updatedAt >= :sinceDate ORDER BY a.updatedAt DESC")
    List<Account> getRecentlyModifiedAccounts(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Count accounts created today
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE DATE(a.createdAt) = CURRENT_DATE")
    Long countAccountsCreatedToday();

    /**
     * Count accounts by user creation date range
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countUserAccountsInDateRange(@Param("userId") Long userId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // ===================== Custom Complex Queries =====================

    /**
     * Get account performance metrics
     */
    @Query("SELECT a.id, a.accountNumber, a.balance, " +
           "(SELECT COUNT(t) FROM Transaction t WHERE (t.fromAccount.id = a.id OR t.toAccount.id = a.id) AND t.createdAt >= :sinceDate) as transactionCount, " +
           "(SELECT SUM(t.amount) FROM Transaction t WHERE t.toAccount.id = a.id AND t.transactionType = 'DEPOSIT' AND t.createdAt >= :sinceDate) as totalDeposits, " +
           "(SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccount.id = a.id AND t.transactionType = 'WITHDRAWAL' AND t.createdAt >= :sinceDate) as totalWithdrawals " +
           "FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    List<Object[]> getAccountPerformanceMetrics(@Param("userId") Long userId, @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Get account transaction summary
     */
    @Query("SELECT a, " +
           "(SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount.id = a.id OR t.toAccount.id = a.id) as totalTransactions, " +
           "(SELECT MAX(t.createdAt) FROM Transaction t WHERE t.fromAccount.id = a.id OR t.toAccount.id = a.id) as lastTransactionDate " +
           "FROM Account a WHERE a.user.id = :userId")
    List<Object[]> getAccountTransactionSummary(@Param("userId") Long userId);

    /**
     * Find accounts with specific transaction patterns
     */
    @Query("SELECT DISTINCT a FROM Account a " +
           "JOIN Transaction t ON (t.fromAccount.id = a.id OR t.toAccount.id = a.id) " +
           "WHERE t.createdAt >= :startDate AND t.amount >= :minAmount " +
           "GROUP BY a.id HAVING COUNT(t) >= :minTransactions")
    List<Account> findAccountsWithTransactionPattern(@Param("startDate") LocalDateTime startDate,
                                                   @Param("minAmount") BigDecimal minAmount,
                                                   @Param("minTransactions") Long minTransactions);

    /**
     * Get account balance history summary
     */
    @Query("SELECT a.id, a.accountNumber, a.balance as currentBalance, " +
           "(SELECT AVG(bh.balance) FROM AccountBalanceHistory bh WHERE bh.accountId = a.id AND bh.recordedAt >= :sinceDate) as avgBalance, " +
           "(SELECT MAX(bh.balance) FROM AccountBalanceHistory bh WHERE bh.accountId = a.id AND bh.recordedAt >= :sinceDate) as maxBalance, " +
           "(SELECT MIN(bh.balance) FROM AccountBalanceHistory bh WHERE bh.accountId = a.id AND bh.recordedAt >= :sinceDate) as minBalance " +
           "FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    List<Object[]> getAccountBalanceHistorySummary(@Param("userId") Long userId, @Param("sinceDate") LocalDateTime sinceDate);
}
