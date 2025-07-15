package com.bankingapp.dto;

import com.bankingapp.entity.Account;

import javax.validation.constraints.NotNull;

public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;

    private String description; // Optional description for account creation

    // Constructors
    public CreateAccountRequest() {}

    public CreateAccountRequest(Account.AccountType accountType) {
        this.accountType = accountType;
    }

    public CreateAccountRequest(Account.AccountType accountType, String description) {
        this.accountType = accountType;
        this.description = description;
    }

    // Getters and Setters
    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CreateAccountRequest{" +
                "accountType=" + accountType +
                ", description='" + description + '\'' +
                '}';
    }
}
