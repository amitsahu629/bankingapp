package com.bankingapp.dto;

import com.bankingapp.entity.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {

    private Long id;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private Long userId;

    private String username; // For display purposes

    private String userFullName; // For display purposes

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal balance;

    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Additional computed fields for frontend
    private String formattedBalance;
    private String accountTypeDisplay;
    private String statusDisplay;
    private Integer transactionCount;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;

    // Constructors
    public AccountDTO() {}

    public AccountDTO(String accountNumber, Account.AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
        this.isActive = true;
    }

    public AccountDTO(Long id, String accountNumber, Long userId, Account.AccountType accountType, 
                     BigDecimal balance, Boolean isActive) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.accountType = accountType;
        this.balance = balance;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { 
        this.accountType = accountType;
        this.accountTypeDisplay = formatAccountType(accountType);
    }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { 
        this.balance = balance;
        this.formattedBalance = formatCurrency(balance);
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        this.statusDisplay = isActive ? "Active" : "Inactive";
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Computed fields
    public String getFormattedBalance() { return formattedBalance; }
    public void setFormattedBalance(String formattedBalance) { this.formattedBalance = formattedBalance; }

    public String getAccountTypeDisplay() { return accountTypeDisplay; }
    public void setAccountTypeDisplay(String accountTypeDisplay) { this.accountTypeDisplay = accountTypeDisplay; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

    public BigDecimal getMonthlySpending() { return monthlySpending; }
    public void setMonthlySpending(BigDecimal monthlySpending) { this.monthlySpending = monthlySpending; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    // Utility methods
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    public boolean isBalancePositive() {
        return balance != null && balance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isBalanceNegative() {
        return balance != null && balance.compareTo(BigDecimal.ZERO) < 0;
    }

    public String getBalanceStatus() {
        if (balance == null) return "unknown";
        if (balance.compareTo(BigDecimal.ZERO) > 0) return "positive";
        if (balance.compareTo(BigDecimal.ZERO) < 0) return "negative";
        return "zero";
    }

    public boolean canWithdraw(BigDecimal amount) {
        if (balance == null || amount == null || !isActive) {
            return false;
        }
        return balance.compareTo(amount) >= 0;
    }

    public boolean canDeposit() {
        return isActive != null && isActive;
    }

    // Helper methods
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    private String formatAccountType(Account.AccountType type) {
        if (type == null) return "Unknown";
        switch (type) {
            case SAVINGS: return "Savings Account";
            case CHECKING: return "Checking Account";
            case CREDIT: return "Credit Account";
            default: return type.name();
        }
    }

    // Static factory methods
    public static AccountDTO fromEntity(Account account) {
        if (account == null) return null;

        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setUserId(account.getUser() != null ? account.getUser().getId() : null);
        dto.setUsername(account.getUser() != null ? account.getUser().getUsername() : null);
        dto.setUserFullName(account.getUser() != null ? 
            account.getUser().getFirstName() + " " + account.getUser().getLastName() : null);
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setIsActive(account.getIsActive());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        
        return dto;
    }

    public Account toEntity() {
        Account account = new Account();
        account.setId(this.id);
        account.setAccountNumber(this.accountNumber);
        account.setAccountType(this.accountType);
        account.setBalance(this.balance != null ? this.balance : BigDecimal.ZERO);
        account.setIsActive(this.isActive != null ? this.isActive : true);
        return account;
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
                "id=" + id +
                ", accountNumber='" + getMaskedAccountNumber() + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", accountType=" + accountType +
                ", balance=" + formattedBalance +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountDTO)) return false;
        AccountDTO that = (AccountDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
}
