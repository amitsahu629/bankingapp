package com.bankingapp.controller;


import com.bankingapp.dto.*;
import com.bankingapp.entity.Account;
import com.bankingapp.security.UserPrincipal;
import com.bankingapp.service.AccountService;
import com.bankingapp.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Account Management Operations
 * Handles CRUD operations for bank accounts
 */
@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Get all accounts for the current user
     */
    @GetMapping
    @Operation(summary = "Get user accounts", description = "Retrieve all accounts belonging to the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<AccountDTO>> getUserAccounts(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        logger.info("Fetching accounts for user: {} (include inactive: {})", 
                   currentUser.getUsername(), includeInactive);

        try {
            List<Account> accounts = includeInactive ? 
                accountService.getAllUserAccounts(currentUser.getId()) :
                accountService.getUserAccounts(currentUser.getId());

            List<AccountDTO> accountDTOs = accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            logger.info("Retrieved {} accounts for user: {}", accountDTOs.size(), currentUser.getUsername());
            return ResponseEntity.ok(accountDTOs);

        } catch (Exception e) {
            logger.error("Error fetching accounts for user: {}", currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get account summaries for dashboard
     */
    @GetMapping("/summary")
    @Operation(summary = "Get account summaries", description = "Get lightweight account summaries for dashboard display")
    public ResponseEntity<List<AccountSummaryDTO>> getAccountSummaries(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Fetching account summaries for user: {}", currentUser.getUsername());

        try {
            List<AccountSummaryDTO> summaries = accountService.getAccountSummaries(currentUser.getId());
            
            logger.info("Retrieved {} account summaries for user: {}", summaries.size(), currentUser.getUsername());
            return ResponseEntity.ok(summaries);

        } catch (Exception e) {
            logger.error("Error fetching account summaries for user: {}", currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific account by ID
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "403", description = "Access denied to this account"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDTO> getAccountById(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Fetching account {} for user: {}", accountId, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to access account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            AccountDTO accountDTO = convertToDTO(account);
            
            // Enrich with transaction count and recent activity
            enrichAccountDTO(accountDTO);
            
            logger.info("Retrieved account {} for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.ok(accountDTO);

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new account
     */
    @PostMapping
    @Operation(summary = "Create new account", description = "Create a new bank account for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Account creation limit reached")
    })
    public ResponseEntity<?> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Creating new {} account for user: {}", 
                   request.getAccountType(), currentUser.getUsername());

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
            
            logger.warn("Validation errors in account creation: {}", errors);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Validation failed", errors));
        }

        try {
            // Check account creation limits
            List<Account> existingAccounts = accountService.getUserAccounts(currentUser.getId());
            if (existingAccounts.size() >= 5) { // Business rule: max 5 accounts per user
                logger.warn("User {} attempted to create more than 5 accounts", currentUser.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Maximum number of accounts (5) reached"));
            }

            Account newAccount = accountService.createAccount(currentUser.getId(), request.getAccountType());
            AccountDTO accountDTO = convertToDTO(newAccount);
            
            logger.info("Successfully created account {} for user: {}", 
                       newAccount.getAccountNumber(), currentUser.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Account created successfully", accountDTO));

        } catch (Exception e) {
            logger.error("Error creating account for user: {}", currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to create account"));
        }
    }

    /**
     * Update account information
     */
    @PutMapping("/{accountId}")
    @Operation(summary = "Update account", description = "Update account information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<?> updateAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Updating account {} for user: {}", accountId, currentUser.getUsername());

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
            
            logger.warn("Validation errors in account update: {}", errors);
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Validation failed", errors));
        }

        try {
            Account existingAccount = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!existingAccount.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to update account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            Account updatedAccount = accountService.updateAccount(accountId, request);
            AccountDTO accountDTO = convertToDTO(updatedAccount);
            
            logger.info("Successfully updated account {} for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Account updated successfully", accountDTO));

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error updating account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to update account"));
        }
    }

    /**
     * Deactivate account (soft delete)
     */
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Deactivate account", description = "Deactivate an account (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot deactivate account with balance"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<?> deactivateAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Deactivating account {} for user: {}", accountId, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to deactivate account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            // Business rule: cannot deactivate account with non-zero balance
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                logger.warn("User {} attempted to deactivate account {} with non-zero balance: {}", 
                           currentUser.getUsername(), accountId, account.getBalance());
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Cannot deactivate account with non-zero balance. " +
                                               "Current balance: $" + account.getBalance()));
            }

            accountService.deactivateAccount(accountId);
            
            logger.info("Successfully deactivated account {} for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Account deactivated successfully"));

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error deactivating account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to deactivate account"));
        }
    }

    /**
     * Reactivate account
     */
    @PutMapping("/{accountId}/activate")
    @Operation(summary = "Reactivate account", description = "Reactivate a previously deactivated account")
    public ResponseEntity<?> reactivateAccount(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Reactivating account {} for user: {}", accountId, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to reactivate account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            accountService.reactivateAccount(accountId);
            
            logger.info("Successfully reactivated account {} for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Account reactivated successfully"));

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error reactivating account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to reactivate account"));
        }
    }

    /**
     * Get account balance
     */
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Get current balance for a specific account")
    public ResponseEntity<?> getAccountBalance(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.debug("Fetching balance for account {} for user: {}", accountId, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to access balance for account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            BalanceDTO balanceDTO = new BalanceDTO(
                accountId,
                account.getAccountNumber(),
                account.getBalance(),
                LocalDateTime.now()
            );
            
            logger.debug("Retrieved balance for account {}: {}", accountId, account.getBalance());
            return ResponseEntity.ok(balanceDTO);

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error fetching balance for account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to fetch account balance"));
        }
    }

    /**
     * Get account statistics
     */
    @GetMapping("/{accountId}/statistics")
    @Operation(summary = "Get account statistics", description = "Get comprehensive statistics for a specific account")
    public ResponseEntity<?> getAccountStatistics(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @RequestParam(defaultValue = "monthly") String period,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Fetching statistics for account {} (period: {}) for user: {}", 
                   accountId, period, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to access statistics for account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            AccountStatisticsDTO statistics = accountService.getAccountStatistics(accountId, period);
            
            logger.info("Retrieved statistics for account {} for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.ok(statistics);

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error fetching statistics for account {} for user: {}", accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to fetch account statistics"));
        }
    }

    /**
     * Get account balance history
     */
    @GetMapping("/{accountId}/balance-history")
    @Operation(summary = "Get balance history", description = "Get balance change history for a specific account")
    public ResponseEntity<?> getBalanceHistory(
            @Parameter(description = "Account ID") @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recordedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Fetching balance history for account {} for user: {}", accountId, currentUser.getUsername());

        try {
            Account account = accountService.getAccountById(accountId);
            
            // Security check: ensure user owns this account
            if (!account.getUser().getId().equals(currentUser.getId())) {
                logger.warn("User {} attempted to access balance history for account {} owned by another user", 
                           currentUser.getUsername(), accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied to this account"));
            }

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<AccountBalanceHistoryDTO> balanceHistory = 
                accountService.getBalanceHistory(accountId, pageable);
            
            logger.info("Retrieved {} balance history records for account {}", 
                       balanceHistory.getContent().size(), accountId);
            return ResponseEntity.ok(balanceHistory);

        } catch (RuntimeException e) {
            logger.error("Account {} not found for user: {}", accountId, currentUser.getUsername());
            return ResponseEntity.notFound()
                .body(new ApiResponse(false, "Account not found"));
        } catch (Exception e) {
            logger.error("Error fetching balance history for account {} for user: {}", 
                        accountId, currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Failed to fetch balance history"));
        }
    }

    /**
     * Search accounts by criteria (Admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search accounts", description = "Search accounts by various criteria (Admin only)")
    public ResponseEntity<Page<AccountDTO>> searchAccounts(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        logger.info("Admin {} searching accounts with criteria", currentUser.getUsername());

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Account> accounts = accountService.searchAccounts(
                accountNumber, accountType, username, isActive, pageable);
            
            Page<AccountDTO> accountDTOs = accounts.map(this::convertToDTO);
            
            logger.info("Admin {} found {} accounts matching criteria", 
                       currentUser.getUsername(), accounts.getTotalElements());
            return ResponseEntity.ok(accountDTOs);

        } catch (Exception e) {
            logger.error("Error in admin account search by user: {}", currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get account types available for creation
     */
    @GetMapping("/types")
    @Operation(summary = "Get account types", description = "Get list of available account types")
    public ResponseEntity<List<AccountTypeDTO>> getAccountTypes() {
        logger.debug("Fetching available account types");
        
        List<AccountTypeDTO> accountTypes = List.of(
            new AccountTypeDTO("SAVINGS", "Savings Account", "Standard savings account with interest"),
            new AccountTypeDTO("CHECKING", "Checking Account", "Everyday spending account with easy access"),
            new AccountTypeDTO("CREDIT", "Credit Account", "Credit line for purchases and cash advances")
        );
        
        return ResponseEntity.ok(accountTypes);
    }

    // Helper methods

    /**
     * Convert Account entity to AccountDTO
     */
    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = AccountDTO.fromEntity(account);
        
        // Add computed fields
        if (dto.getBalance() != null) {
            dto.setFormattedBalance(String.format("$%,.2f", dto.getBalance()));
        }
        
        dto.setAccountTypeDisplay(formatAccountType(account.getAccountType()));
        dto.setStatusDisplay(account.getIsActive() ? "Active" : "Inactive");
        
        return dto;
    }

    /**
     * Enrich AccountDTO with additional information
     */
    private void enrichAccountDTO(AccountDTO accountDTO) {
        try {
            // Add transaction count
            Integer transactionCount = transactionService.getTransactionCount(accountDTO.getId());
            accountDTO.setTransactionCount(transactionCount);
            
            // Add monthly statistics
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            BigDecimal monthlySpending = transactionService.getMonthlySpending(accountDTO.getId(), monthStart);
            BigDecimal monthlyIncome = transactionService.getMonthlyIncome(accountDTO.getId(), monthStart);
            
            accountDTO.setMonthlySpending(monthlySpending);
            accountDTO.setMonthlyIncome(monthlyIncome);
            
        } catch (Exception e) {
            logger.warn("Failed to enrich account DTO for account {}: {}", accountDTO.getId(), e.getMessage());
            // Set default values if enrichment fails
            accountDTO.setTransactionCount(0);
            accountDTO.setMonthlySpending(BigDecimal.ZERO);
            accountDTO.setMonthlyIncome(BigDecimal.ZERO);
        }
    }

    /**
     * Format account type for display
     */
    private String formatAccountType(Account.AccountType accountType) {
        switch (accountType) {
            case SAVINGS: return "Savings Account";
            case CHECKING: return "Checking Account";
            case CREDIT: return "Credit Account";
            default: return accountType.name();
        }
    }

    // Response DTOs

    /**
     * Generic API response wrapper
     */
    public static class ApiResponse {
        private Boolean success;
        private String message;
        private Object data;
        private List<String> errors;
        private LocalDateTime timestamp;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public ApiResponse(Boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        public ApiResponse(Boolean success, String message, List<String> errors) {
            this.success = success;
            this.message = message;
            this.errors = errors;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Balance response DTO
     */
    public static class BalanceDTO {
        private Long accountId;
        private String accountNumber;
        private BigDecimal balance;
        private String formattedBalance;
        private LocalDateTime lastUpdated;

        public BalanceDTO(Long accountId, String accountNumber, BigDecimal balance, LocalDateTime lastUpdated) {
            this.accountId = accountId;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.formattedBalance = String.format("$%,.2f", balance);
            this.lastUpdated = lastUpdated;
        }

        // Getters and setters
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { 
            this.balance = balance;
            this.formattedBalance = String.format("$%,.2f", balance);
        }

        public String getFormattedBalance() { return formattedBalance; }
        public void setFormattedBalance(String formattedBalance) { this.formattedBalance = formattedBalance; }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

        public String getMaskedAccountNumber() {
            if (accountNumber == null || accountNumber.length() < 4) {
                return accountNumber;
            }
            String lastFour = accountNumber.substring(accountNumber.length() - 4);
            return "****" + lastFour;
        }
    }

    /**
     * Account type information DTO
     */
    public static class AccountTypeDTO {
        private String code;
        private String name;
        private String description;

        public AccountTypeDTO(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}