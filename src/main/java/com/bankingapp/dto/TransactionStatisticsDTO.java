package com.bankingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for transaction analytics and statistics
 */
public class TransactionStatisticsDTO {

    private Long accountId;
    private String accountNumber;
    private String period; // "daily", "weekly", "monthly", "yearly"

    // Transaction counts
    private Integer totalTransactions;
    private Integer depositsCount;
    private Integer withdrawalsCount;
    private Integer transfersCount;
    private Integer pendingCount;
    private Integer completedCount;
    private Integer failedCount;

    // Amount statistics
    private BigDecimal totalAmount;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfers;
    private BigDecimal averageTransactionAmount;
    private BigDecimal largestTransaction;
    private BigDecimal smallestTransaction;

    // Formatted amounts
    private String formattedTotalAmount;
    private String formattedTotalDeposits;
    private String formattedTotalWithdrawals;
    private String formattedAverageAmount;

    // Date information
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime firstTransactionDate;

    // Trends and analysis
    private BigDecimal netCashFlow; // deposits - withdrawals
    private String formattedNetCashFlow;
    private Double transactionFrequency; // transactions per day
    private String mostFrequentTransactionType;
    private String busyDay; // day of week with most transactions
    private String busyHour; // hour of day with most transactions

    // Comparison with previous period
    private BigDecimal percentageChange;
    private String changeDirection; // "up", "down", "same"
    private String trendAnalysis; // "increasing", "decreasing", "stable"

    // Category breakdown
    private Map<String, Integer> transactionsByType;
    private Map<String, BigDecimal> amountsByType;
    private Map<String, Integer> transactionsByDay;
    private Map<String, Integer> transactionsByHour;

    // Constructors
    public TransactionStatisticsDTO() {}

    public TransactionStatisticsDTO(Long accountId, String period) {
        this.accountId = accountId;
        this.period = period;
    }

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public Integer getDepositsCount() { return depositsCount; }
    public void setDepositsCount(Integer depositsCount) { this.depositsCount = depositsCount; }

    public Integer getWithdrawalsCount() { return withdrawalsCount; }
    public void setWithdrawalsCount(Integer withdrawalsCount) { this.withdrawalsCount = withdrawalsCount; }

    public Integer getTransfersCount() { return transfersCount; }
    public void setTransfersCount(Integer transfersCount) { this.transfersCount = transfersCount; }

    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { 
        this.totalAmount = totalAmount;
        this.formattedTotalAmount = formatCurrency(totalAmount);
    }

    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(BigDecimal totalDeposits) { 
        this.totalDeposits = totalDeposits;
        this.formattedTotalDeposits = formatCurrency(totalDeposits);
    }

    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) { 
        this.totalWithdrawals = totalWithdrawals;
        this.formattedTotalWithdrawals = formatCurrency(totalWithdrawals);
    }

    public BigDecimal getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(BigDecimal totalTransfers) { this.totalTransfers = totalTransfers; }

    public BigDecimal getAverageTransactionAmount() { return averageTransactionAmount; }
    public void setAverageTransactionAmount(BigDecimal averageTransactionAmount) { 
        this.averageTransactionAmount = averageTransactionAmount;
        this.formattedAverageAmount = formatCurrency(averageTransactionAmount);
    }

    public BigDecimal getLargestTransaction() { return largestTransaction; }
    public void setLargestTransaction(BigDecimal largestTransaction) { this.largestTransaction = largestTransaction; }

    public BigDecimal getSmallestTransaction() { return smallestTransaction; }
    public void setSmallestTransaction(BigDecimal smallestTransaction) { this.smallestTransaction = smallestTransaction; }

    // Formatted amounts
    public String getFormattedTotalAmount() { return formattedTotalAmount; }
    public String getFormattedTotalDeposits() { return formattedTotalDeposits; }
    public String getFormattedTotalWithdrawals() { return formattedTotalWithdrawals; }
    public String getFormattedAverageAmount() { return formattedAverageAmount; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public LocalDateTime getFirstTransactionDate() { return firstTransactionDate; }
    public void setFirstTransactionDate(LocalDateTime firstTransactionDate) { this.firstTransactionDate = firstTransactionDate; }

    public BigDecimal getNetCashFlow() { return netCashFlow; }
    public void setNetCashFlow(BigDecimal netCashFlow) { 
        this.netCashFlow = netCashFlow;
        this.formattedNetCashFlow = formatCurrency(netCashFlow);
    }

    public String getFormattedNetCashFlow() { return formattedNetCashFlow; }

    public Double getTransactionFrequency() { return transactionFrequency; }
    public void setTransactionFrequency(Double transactionFrequency) { this.transactionFrequency = transactionFrequency; }

    public String getMostFrequentTransactionType() { return mostFrequentTransactionType; }
    public void setMostFrequentTransactionType(String mostFrequentTransactionType) { this.mostFrequentTransactionType = mostFrequentTransactionType; }

    public String getBusyDay() { return busyDay; }
    public void setBusyDay(String busyDay) { this.busyDay = busyDay; }

    public String getBusyHour() { return busyHour; }
    public void setBusyHour(String busyHour) { this.busyHour = busyHour; }

    public BigDecimal getPercentageChange() { return percentageChange; }
    public void setPercentageChange(BigDecimal percentageChange) { this.percentageChange = percentageChange; }

    public String getChangeDirection() { return changeDirection; }
    public void setChangeDirection(String changeDirection) { this.changeDirection = changeDirection; }

    public String getTrendAnalysis() { return trendAnalysis; }
    public void setTrendAnalysis(String trendAnalysis) { this.trendAnalysis = trendAnalysis; }

    public Map<String, Integer> getTransactionsByType() { return transactionsByType; }
    public void setTransactionsByType(Map<String, Integer> transactionsByType) { this.transactionsByType = transactionsByType; }

    public Map<String, BigDecimal> getAmountsByType() { return amountsByType; }
    public void setAmountsByType(Map<String, BigDecimal> amountsByType) { this.amountsByType = amountsByType; }

    public Map<String, Integer> getTransactionsByDay() { return transactionsByDay; }
    public void setTransactionsByDay(Map<String, Integer> transactionsByDay) { this.transactionsByDay = transactionsByDay; }

    public Map<String, Integer> getTransactionsByHour() { return transactionsByHour; }
    public void setTransactionsByHour(Map<String, Integer> transactionsByHour) { this.transactionsByHour = transactionsByHour; }

    // Utility methods
    public boolean isPositiveCashFlow() {
        return netCashFlow != null && netCashFlow.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegativeCashFlow() {
        return netCashFlow != null && netCashFlow.compareTo(BigDecimal.ZERO) < 0;
    }

    public double getSuccessRate() {
        if (totalTransactions == null || totalTransactions == 0) return 0.0;
        int completed = completedCount != null ? completedCount : 0;
        return (double) completed / totalTransactions * 100;
    }

    public double getFailureRate() {
        if (totalTransactions == null || totalTransactions == 0) return 0.0;
        int failed = failedCount != null ? failedCount : 0;
        return (double) failed / totalTransactions * 100;
    }

    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    public String getHealthStatus() {
        double successRate = getSuccessRate();
        if (successRate >= 95) return "Excellent";
        if (successRate >= 90) return "Good";
        if (successRate >= 80) return "Fair";
        return "Poor";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "TransactionStatisticsDTO{" +
                "accountId=" + accountId +
                ", period='" + period + '\'' +
                ", totalTransactions=" + totalTransactions +
                ", totalAmount=" + formattedTotalAmount +
                ", netCashFlow=" + formattedNetCashFlow +
                ", successRate=" + String.format("%.1f%%", getSuccessRate()) +
                '}';
    }
}

