package com.bankingapp.dto;

import com.bankingapp.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for filtering and searching transactions
 */
public class TransactionFilterDTO {

    private Long accountId;
    private Transaction.TransactionType transactionType;
    private Transaction.TransactionStatus status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private String description;
    private String transactionId;
    private String sortBy = "createdAt"; // createdAt, amount, status
    private String sortDirection = "DESC"; // ASC, DESC
    private Integer page = 0;
    private Integer size = 20;

    // Constructors
    public TransactionFilterDTO() {}

    public TransactionFilterDTO(Long accountId) {
        this.accountId = accountId;
    }

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Transaction.TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(Transaction.TransactionType transactionType) { this.transactionType = transactionType; }

    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { this.status = status; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    // Validation methods
    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    public boolean hasAmountRange() {
        return minAmount != null && maxAmount != null;
    }

    public boolean isValidDateRange() {
        return !hasDateRange() || startDate.isBefore(endDate);
    }

    public boolean isValidAmountRange() {
        return !hasAmountRange() || minAmount.compareTo(maxAmount) <= 0;
    }

    public boolean isEmpty() {
        return accountId == null && transactionType == null && status == null &&
               minAmount == null && maxAmount == null && startDate == null &&
               endDate == null && (description == null || description.trim().isEmpty()) &&
               (transactionId == null || transactionId.trim().isEmpty());
    }

    @Override
    public String toString() {
        return "TransactionFilterDTO{" +
                "accountId=" + accountId +
                ", transactionType=" + transactionType +
                ", status=" + status +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
