package com.bankingapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for transaction summaries and lists
 */
public class TransactionSummaryDTO {

    private Long id;
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String formattedAmount;
    private String status;
    private String statusColor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String formattedDate;
    private String description;
    private String direction; // "in", "out", "internal"
    private String icon;
    private String otherParty; // Other account involved in transaction

    // Constructors
    public TransactionSummaryDTO() {}

    public TransactionSummaryDTO(String transactionId, String type, BigDecimal amount, String status) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.formattedAmount = formatCurrency(amount);
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        this.formattedAmount = formatCurrency(amount);
    }

    public String getFormattedAmount() { return formattedAmount; }
    public void setFormattedAmount(String formattedAmount) { this.formattedAmount = formattedAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getOtherParty() { return otherParty; }
    public void setOtherParty(String otherParty) { this.otherParty = otherParty; }

    // Utility methods
    public String getMaskedTransactionId() {
        if (transactionId == null || transactionId.length() < 8) {
            return transactionId;
        }
        return transactionId.substring(0, 4) + "****" + transactionId.substring(transactionId.length() - 4);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    @Override
    public String toString() {
        return "TransactionSummaryDTO{" +
                "transactionId='" + getMaskedTransactionId() + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + formattedAmount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

