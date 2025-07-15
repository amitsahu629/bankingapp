package com.bankingapp.dto;

import com.bankingapp.entity.Account;

import javax.validation.constraints.Size;

public class UpdateAccountRequest {

    private Account.AccountType accountType;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private Boolean isActive;

    // Constructors
    public UpdateAccountRequest() {}

    public UpdateAccountRequest(Account.AccountType accountType, Boolean isActive) {
        this.accountType = accountType;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "UpdateAccountRequest{" +
                "accountType=" + accountType +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
