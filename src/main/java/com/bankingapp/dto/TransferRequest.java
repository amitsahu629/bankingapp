package com.bankingapp.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for transfer requests between accounts
 */
public class TransferRequest {

    @NotNull(message = "Source account ID is required")
    private Long fromAccountId;

    @NotNull(message = "Destination account ID is required")
    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private String reference; // External reference number
    private Boolean sendNotification = true; // Whether to send notification
    private String transferType = "IMMEDIATE"; // IMMEDIATE, SCHEDULED, RECURRING
    private String scheduledDate; // For scheduled transfers
    private String frequency; // For recurring transfers (DAILY, WEEKLY, MONTHLY)

    // Constructors
    public TransferRequest() {}

    public TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    public TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Boolean getSendNotification() { return sendNotification; }
    public void setSendNotification(Boolean sendNotification) { this.sendNotification = sendNotification; }

    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    // Validation methods
    public boolean isValidTransfer() {
        return fromAccountId != null && toAccountId != null && 
               !fromAccountId.equals(toAccountId) && amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSelfTransfer() {
        return fromAccountId != null && fromAccountId.equals(toAccountId);
    }

    public boolean isScheduled() {
        return "SCHEDULED".equals(transferType);
    }

    public boolean isRecurring() {
        return "RECURRING".equals(transferType);
    }

    // Utility methods
    public String getFormattedAmount() {
        return amount != null ? String.format("$%,.2f", amount) : "$0.00";
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", amount=" + getFormattedAmount() +
                ", description='" + description + '\'' +
                ", transferType='" + transferType + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
