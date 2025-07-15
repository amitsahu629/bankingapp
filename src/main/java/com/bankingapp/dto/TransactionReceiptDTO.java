package com.bankingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction receipts and confirmations
 */
public class TransactionReceiptDTO {

    private String transactionId;
    private String receiptNumber;
    private String transactionType;
    private BigDecimal amount;
    private String formattedAmount;
    private BigDecimal fee;
    private String formattedFee;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;

    private String fromAccountNumber;
    private String fromAccountType;
    private String fromAccountHolder;
    private BigDecimal fromAccountBalanceBefore;
    private BigDecimal fromAccountBalanceAfter;

    private String toAccountNumber;
    private String toAccountType;
    private String toAccountHolder;
    private BigDecimal toAccountBalanceBefore;
    private BigDecimal toAccountBalanceAfter;

    private String status;
    private String description;
    private String reference;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedDate;

    private String bankName;
    private String bankAddress;
    private String customerServicePhone;
    private String disclaimerText;

    // Constructors
    public TransactionReceiptDTO() {}

    public TransactionReceiptDTO(String transactionId, String transactionType, BigDecimal amount) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.formattedAmount = formatCurrency(amount);
        this.receiptNumber = generateReceiptNumber();
        this.transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        this.formattedAmount = formatCurrency(amount);
        calculateTotalAmount();
    }

    public String getFormattedAmount() { return formattedAmount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { 
        this.fee = fee;
        this.formattedFee = formatCurrency(fee);
        calculateTotalAmount();
    }

    public String getFormattedFee() { return formattedFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getFormattedTotalAmount() { return formattedTotalAmount; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getFromAccountType() { return fromAccountType; }
    public void setFromAccountType(String fromAccountType) { this.fromAccountType = fromAccountType; }

    public String getFromAccountHolder() { return fromAccountHolder; }
    public void setFromAccountHolder(String fromAccountHolder) { this.fromAccountHolder = fromAccountHolder; }

    public BigDecimal getFromAccountBalanceBefore() { return fromAccountBalanceBefore; }
    public void setFromAccountBalanceBefore(BigDecimal fromAccountBalanceBefore) { this.fromAccountBalanceBefore = fromAccountBalanceBefore; }

    public BigDecimal getFromAccountBalanceAfter() { return fromAccountBalanceAfter; }
    public void setFromAccountBalanceAfter(BigDecimal fromAccountBalanceAfter) { this.fromAccountBalanceAfter = fromAccountBalanceAfter; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public String getToAccountType() { return toAccountType; }
    public void setToAccountType(String toAccountType) { this.toAccountType = toAccountType; }

    public String getToAccountHolder() { return toAccountHolder; }
    public void setToAccountHolder(String toAccountHolder) { this.toAccountHolder = toAccountHolder; }

    public BigDecimal getToAccountBalanceBefore() { return toAccountBalanceBefore; }
    public void setToAccountBalanceBefore(BigDecimal toAccountBalanceBefore) { this.toAccountBalanceBefore = toAccountBalanceBefore; }

    public BigDecimal getToAccountBalanceAfter() { return toAccountBalanceAfter; }
    public void setToAccountBalanceAfter(BigDecimal toAccountBalanceAfter) { this.toAccountBalanceAfter = toAccountBalanceAfter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAddress() { return bankAddress; }
    public void setBankAddress(String bankAddress) { this.bankAddress = bankAddress; }

    public String getCustomerServicePhone() { return customerServicePhone; }
    public void setCustomerServicePhone(String customerServicePhone) { this.customerServicePhone = customerServicePhone; }

    public String getDisclaimerText() { return disclaimerText; }
    public void setDisclaimerText(String disclaimerText) { this.disclaimerText = disclaimerText; }

    // Utility methods
    public String getMaskedFromAccount() {
        return maskAccountNumber(fromAccountNumber);
    }

    public String getMaskedToAccount() {
        return maskAccountNumber(toAccountNumber);
    }

    public String getFormattedFromBalanceBefore() {
        return formatCurrency(fromAccountBalanceBefore);
    }

    public String getFormattedFromBalanceAfter() {
        return formatCurrency(fromAccountBalanceAfter);
    }

    public String getFormattedToBalanceBefore() {
        return formatCurrency(toAccountBalanceBefore);
    }

    public String getFormattedToBalanceAfter() {
        return formatCurrency(toAccountBalanceAfter);
    }

    public boolean hasFromAccount() {
        return fromAccountNumber != null;
    }

    public boolean hasToAccount() {
        return toAccountNumber != null;
    }

    public boolean hasFee() {
        return fee != null && fee.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSuccessful() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    private void calculateTotalAmount() {
        BigDecimal base = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal feeAmount = fee != null ? fee : BigDecimal.ZERO;
        this.totalAmount = base.add(feeAmount);
        this.formattedTotalAmount = formatCurrency(totalAmount);
    }

    private String generateReceiptNumber() {
        return "RCP-" + System.currentTimeMillis();
    }

    private String maskAccountNumber(String accountNumber) {
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
        return "TransactionReceiptDTO{" +
                "transactionId='" + transactionId + '\'' +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + formattedAmount +
                ", status='" + status + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}