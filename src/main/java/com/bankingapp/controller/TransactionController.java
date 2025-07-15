package com.bankingapp.controller;

import com.bankingapp.dto.TransactionRequest;
import com.bankingapp.dto.TransferRequest;
import com.bankingapp.entity.Transaction;
import com.bankingapp.security.UserPrincipal;
import com.bankingapp.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody TransactionRequest request,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Deposit request from user: {}", currentUser.getUsername());
        
        Transaction transaction = transactionService.deposit(
            request.getAccountId(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@Valid @RequestBody TransactionRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Withdrawal request from user: {}", currentUser.getUsername());
        
        Transaction transaction = transactionService.withdraw(
            request.getAccountId(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@Valid @RequestBody TransferRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Transfer request from user: {}", currentUser.getUsername());
        
        Transaction transaction = transactionService.transfer(
            request.getFromAccountId(),
            request.getToAccountId(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable Long accountId,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Transaction history request for account: {} by user: {}", accountId, currentUser.getUsername());
        
        List<Transaction> transactions = transactionService.getTransactionHistory(accountId);
        
        return ResponseEntity.ok(transactions);
    }
}
