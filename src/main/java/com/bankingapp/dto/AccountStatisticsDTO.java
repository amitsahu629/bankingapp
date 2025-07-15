package com.bankingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account statistics and analytics
 */
public class AccountStatisticsDTO {

    private Long accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal averageBalance;
    private BigDecimal highestBalance;
    private BigDecimal lowestBalance;
    
    // Transaction statistics
    private Integer totalTransactions;
    private Integer depositsCount;
    private Integer withdrawalsCount;
    private Integer transfersCount;
    
    // Amount statistics
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfers;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;
    
    // Date information
    private LocalDateTime firstTransactionDate;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime accountCreatedDate;
    
    // Performance metrics
    private BigDecimal transactionFrequency; // transactions per month
    private String accountHealthStatus; // healthy, moderate, concerning

    // Constructors
    public AccountStatisticsDTO() {}

    public AccountStatisticsDTO(Long accountId, String accountNumber, String accountType, BigDecimal currentBalance) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currentBalance = currentBalance;
    }

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getAverageBalance() { return averageBalance; }
    public void setAverageBalance(BigDecimal averageBalance) { this.averageBalance = averageBalance; }

    public BigDecimal getHighestBalance() { return highestBalance; }
    public void setHighestBalance(BigDecimal highestBalance) { this.highestBalance = highestBalance; }

    public BigDecimal getLowestBalance() { return lowestBalance; }
    public void setLowestBalance(BigDecimal lowestBalance) { this.lowestBalance = lowestBalance; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public Integer getDepositsCount() { return depositsCount; }
    public void setDepositsCount(Integer depositsCount) { this.depositsCount = depositsCount; }

    public Integer getWithdrawalsCount() { return withdrawalsCount; }
    public void setWithdrawalsCount(Integer withdrawalsCount) { this.withdrawalsCount = withdrawalsCount; }

    public Integer getTransfersCount() { return transfersCount; }
    public void setTransfersCount(Integer transfersCount) { this.transfersCount = transfersCount; }

    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }

    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }

    public BigDecimal getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(BigDecimal totalTransfers) { this.totalTransfers = totalTransfers; }

    public BigDecimal getMonthlySpending() { return monthlySpending; }
    public void setMonthlySpending(BigDecimal monthlySpending) { this.monthlySpending = monthlySpending; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public LocalDateTime getFirstTransactionDate() { return firstTransactionDate; }
    public void setFirstTransactionDate(LocalDateTime firstTransactionDate) { this.firstTransactionDate = firstTransactionDate; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public LocalDateTime getAccountCreatedDate() { return accountCreatedDate; }
    public void setAccountCreatedDate(LocalDateTime accountCreatedDate) { this.accountCreatedDate = accountCreatedDate; }

    public BigDecimal getTransactionFrequency() { return transactionFrequency; }
    public void setTransactionFrequency(BigDecimal transactionFrequency) { this.transactionFrequency = transactionFrequency; }

    public String getAccountHealthStatus() { return accountHealthStatus; }
    public void setAccountHealthStatus(String accountHealthStatus) { this.accountHealthStatus = accountHealthStatus; }

    // Utility methods
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    public BigDecimal getNetCashFlow() {
        BigDecimal income = totalDeposits != null ? totalDeposits : BigDecimal.ZERO;
        BigDecimal outgoing = totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO;
        return income.subtract(outgoing);
    }

    public boolean isHealthy() {
        return "healthy".equalsIgnoreCase(accountHealthStatus);
    }

    public String getFormattedCurrentBalance() {
        return formatCurrency(currentBalance);
    }

    public String getFormattedAverageBalance() {
        return formatCurrency(averageBalance);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "AccountStatisticsDTO{" +
                "accountId=" + accountId +
                ", accountNumber='" + getMaskedAccountNumber() + '\'' +
                ", accountType='" + accountType + '\'' +
                ", currentBalance=" + getFormattedCurrentBalance() +
                ", totalTransactions=" + totalTransactions +
                ", accountHealthStatus='" + accountHealthStatus + '\'' +
                '}';
    }
}
