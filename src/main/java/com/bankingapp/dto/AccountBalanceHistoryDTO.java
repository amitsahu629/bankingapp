package com.bankingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account balance history tracking
 */
public class AccountBalanceHistoryDTO {

    private Long id;
    private Long accountId;
    private BigDecimal balance;
    private String formattedBalance;
    private String changeType; // DEPOSIT, WITHDRAWAL, TRANSFER, ADJUSTMENT
    private BigDecimal changeAmount;
    private String formattedChangeAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    private String transactionReference;
    private String description;

    // Constructors
    public AccountBalanceHistoryDTO() {}

    public AccountBalanceHistoryDTO(Long accountId, BigDecimal balance, String changeType, BigDecimal changeAmount) {
        this.accountId = accountId;
        this.balance = balance;
        this.changeType = changeType;
        this.changeAmount = changeAmount;
        this.formattedBalance = formatCurrency(balance);
        this.formattedChangeAmount = formatCurrency(changeAmount);
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { 
        this.balance = balance;
        this.formattedBalance = formatCurrency(balance);
    }

    public String getFormattedBalance() { return formattedBalance; }
    public void setFormattedBalance(String formattedBalance) { this.formattedBalance = formattedBalance; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { 
        this.changeAmount = changeAmount;
        this.formattedChangeAmount = formatCurrency(changeAmount);
    }

    public String getFormattedChangeAmount() { return formattedChangeAmount; }
    public void setFormattedChangeAmount(String formattedChangeAmount) { this.formattedChangeAmount = formattedChangeAmount; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Utility methods
    public boolean isIncrease() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDecrease() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    public String getChangeDirection() {
        if (isIncrease()) return "increase";
        if (isDecrease()) return "decrease";
        return "no-change";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "AccountBalanceHistoryDTO{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", balance=" + formattedBalance +
                ", changeType='" + changeType + '\'' +
                ", changeAmount=" + formattedChangeAmount +
                ", recordedAt=" + recordedAt +
                '}';
    }
}
