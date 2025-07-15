package com.bankingapp.dto;

import com.bankingapp.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    private Long id;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    private Long fromAccountId;
    private String fromAccountNumber;
    private String fromAccountType;
    private String fromAccountOwner;

    private Long toAccountId;
    private String toAccountNumber;
    private String toAccountType;
    private String toAccountOwner;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Transaction status is required")
    private Transaction.TransactionStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    // Additional fields for display and analytics
    private String formattedAmount;
    private String transactionTypeDisplay;
    private String statusDisplay;
    private String formattedDate;
    private String formattedTime;
    private String direction; // "incoming", "outgoing", "internal"
    private String category; // "transfer", "payment", "deposit", etc.
    private String icon; // For frontend display
    private String statusColor; // For frontend styling
    private BigDecimal balanceAfter; // Account balance after transaction
    private String formattedBalanceAfter;
    private String reference; // External reference number
    private String initiatedBy; // Username who initiated
    private String approvedBy; // Username who approved (if applicable)

    // Fee information
    private BigDecimal fee;
    private String formattedFee;
    private String feeDescription;

    // Error information (for failed transactions)
    private String errorCode;
    private String errorMessage;

    // Constructors
    public TransactionDTO() {}

    public TransactionDTO(String transactionId, Transaction.TransactionType transactionType, 
                         BigDecimal amount, String description) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.status = Transaction.TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.formattedAmount = formatCurrency(amount);
        this.transactionTypeDisplay = formatTransactionType(transactionType);
        this.statusDisplay = formatStatus(status);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getFromAccountType() { return fromAccountType; }
    public void setFromAccountType(String fromAccountType) { this.fromAccountType = fromAccountType; }

    public String getFromAccountOwner() { return fromAccountOwner; }
    public void setFromAccountOwner(String fromAccountOwner) { this.fromAccountOwner = fromAccountOwner; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public String getToAccountType() { return toAccountType; }
    public void setToAccountType(String toAccountType) { this.toAccountType = toAccountType; }

    public String getToAccountOwner() { return toAccountOwner; }
    public void setToAccountOwner(String toAccountOwner) { this.toAccountOwner = toAccountOwner; }

    public Transaction.TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(Transaction.TransactionType transactionType) { 
        this.transactionType = transactionType;
        this.transactionTypeDisplay = formatTransactionType(transactionType);
        this.category = determineCategory(transactionType);
        this.icon = determineIcon(transactionType);
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        this.formattedAmount = formatCurrency(amount);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Transaction.TransactionStatus getStatus() { return status; }
    public void setStatus(Transaction.TransactionStatus status) { 
        this.status = status;
        this.statusDisplay = formatStatus(status);
        this.statusColor = getStatusColor(status);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.formattedDate = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.formattedTime = createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Display and computed fields
    public String getFormattedAmount() { return formattedAmount; }
    public void setFormattedAmount(String formattedAmount) { this.formattedAmount = formattedAmount; }

    public String getTransactionTypeDisplay() { return transactionTypeDisplay; }
    public void setTransactionTypeDisplay(String transactionTypeDisplay) { this.transactionTypeDisplay = transactionTypeDisplay; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public String getFormattedTime() { return formattedTime; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { 
        this.balanceAfter = balanceAfter;
        this.formattedBalanceAfter = formatCurrency(balanceAfter);
    }

    public String getFormattedBalanceAfter() { return formattedBalanceAfter; }
    public void setFormattedBalanceAfter(String formattedBalanceAfter) { this.formattedBalanceAfter = formattedBalanceAfter; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { 
        this.fee = fee;
        this.formattedFee = formatCurrency(fee);
    }

    public String getFormattedFee() { return formattedFee; }
    public void setFormattedFee(String formattedFee) { this.formattedFee = formattedFee; }

    public String getFeeDescription() { return feeDescription; }
    public void setFeeDescription(String feeDescription) { this.feeDescription = feeDescription; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Utility methods
    public boolean isCompleted() {
        return Transaction.TransactionStatus.COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return Transaction.TransactionStatus.FAILED.equals(status);
    }

    public boolean isPending() {
        return Transaction.TransactionStatus.PENDING.equals(status);
    }

    public boolean hasError() {
        return errorCode != null || errorMessage != null;
    }

    public boolean hasFee() {
        return fee != null && fee.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;
        if (hasFee()) {
            total = total.add(fee);
        }
        return total;
    }

    public String getFormattedTotalAmount() {
        return formatCurrency(getTotalAmount());
    }

    public String getMaskedFromAccount() {
        return maskAccountNumber(fromAccountNumber);
    }

    public String getMaskedToAccount() {
        return maskAccountNumber(toAccountNumber);
    }

    public String getDisplayTitle() {
        switch (transactionType) {
            case DEPOSIT:
                return "Money Deposited";
            case WITHDRAWAL:
                return "Money Withdrawn";
            case TRANSFER:
                return "Money Transferred";
            default:
                return "Transaction";
        }
    }

    public String getDisplayDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        
        switch (transactionType) {
            case DEPOSIT:
                return "Deposit to " + getMaskedToAccount();
            case WITHDRAWAL:
                return "Withdrawal from " + getMaskedFromAccount();
            case TRANSFER:
                return "Transfer from " + getMaskedFromAccount() + " to " + getMaskedToAccount();
            default:
                return "Transaction " + transactionId;
        }
    }

    public long getProcessingTimeMinutes() {
        if (createdAt != null && completedAt != null) {
            return java.time.Duration.between(createdAt, completedAt).toMinutes();
        }
        return 0;
    }

    // Helper methods
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    private String formatTransactionType(Transaction.TransactionType type) {
        if (type == null) return "Unknown";
        switch (type) {
            case DEPOSIT: return "Deposit";
            case WITHDRAWAL: return "Withdrawal";
            case TRANSFER: return "Transfer";
            default: return type.name();
        }
    }

    private String formatStatus(Transaction.TransactionStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case PENDING: return "Pending";
            case COMPLETED: return "Completed";
            case FAILED: return "Failed";
            default: return status.name();
        }
    }

    private String getStatusColor(Transaction.TransactionStatus status) {
        if (status == null) return "gray";
        switch (status) {
            case PENDING: return "orange";
            case COMPLETED: return "green";
            case FAILED: return "red";
            default: return "gray";
        }
    }

    private String determineCategory(Transaction.TransactionType type) {
        if (type == null) return "other";
        switch (type) {
            case DEPOSIT: return "income";
            case WITHDRAWAL: return "expense";
            case TRANSFER: return "transfer";
            default: return "other";
        }
    }

    private String determineIcon(Transaction.TransactionType type) {
        if (type == null) return "fas fa-question";
        switch (type) {
            case DEPOSIT: return "fas fa-plus-circle";
            case WITHDRAWAL: return "fas fa-minus-circle";
            case TRANSFER: return "fas fa-exchange-alt";
            default: return "fas fa-question";
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }

    // Static factory methods
    public static TransactionDTO fromEntity(Transaction transaction) {
        if (transaction == null) return null;

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        
        if (transaction.getFromAccount() != null) {
            dto.setFromAccountId(transaction.getFromAccount().getId());
            dto.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
            dto.setFromAccountType(transaction.getFromAccount().getAccountType().name());
            if (transaction.getFromAccount().getUser() != null) {
                dto.setFromAccountOwner(transaction.getFromAccount().getUser().getFirstName() + " " + 
                                      transaction.getFromAccount().getUser().getLastName());
            }
        }
        
        if (transaction.getToAccount() != null) {
            dto.setToAccountId(transaction.getToAccount().getId());
            dto.setToAccountNumber(transaction.getToAccount().getAccountNumber());
            dto.setToAccountType(transaction.getToAccount().getAccountType().name());
            if (transaction.getToAccount().getUser() != null) {
                dto.setToAccountOwner(transaction.getToAccount().getUser().getFirstName() + " " + 
                                    transaction.getToAccount().getUser().getLastName());
            }
        }
        
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        
        return dto;
    }

    public Transaction toEntity() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setTransactionId(this.transactionId);
        transaction.setTransactionType(this.transactionType);
        transaction.setAmount(this.amount);
        transaction.setDescription(this.description);
        transaction.setStatus(this.status);
        transaction.setCreatedAt(this.createdAt);
        return transaction;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", transactionType=" + transactionType +
                ", amount=" + formattedAmount +
                ", status=" + status +
                ", fromAccount='" + getMaskedFromAccount() + '\'' +
                ", toAccount='" + getMaskedToAccount() + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionDTO)) return false;
        TransactionDTO that = (TransactionDTO) o;
        return transactionId != null && transactionId.equals(that.transactionId);
    }

    @Override
    public int hashCode() {
        return transactionId != null ? transactionId.hashCode() : 0;
    }
}

