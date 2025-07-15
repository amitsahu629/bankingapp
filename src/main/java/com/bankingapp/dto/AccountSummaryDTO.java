package com.bankingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for account summaries and lists
 */
public class AccountSummaryDTO {

    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String formattedBalance;
    private Boolean isActive;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastTransactionDate;

    // Constructors
    public AccountSummaryDTO() {}

    public AccountSummaryDTO(Long id, String accountNumber, String accountType, 
                           BigDecimal balance, Boolean isActive) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.isActive = isActive;
        this.formattedBalance = formatCurrency(balance);
        this.status = isActive ? "Active" : "Inactive";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { 
        this.balance = balance;
        this.formattedBalance = formatCurrency(balance);
    }

    public String getFormattedBalance() { return formattedBalance; }
    public void setFormattedBalance(String formattedBalance) { this.formattedBalance = formattedBalance; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        this.status = isActive ? "Active" : "Inactive";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    // Utility methods
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "AccountSummaryDTO{" +
                "id=" + id +
                ", accountNumber='" + getMaskedAccountNumber() + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + formattedBalance +
                ", isActive=" + isActive +
                ", lastTransactionDate=" + lastTransactionDate +
                '}';
    }
}

