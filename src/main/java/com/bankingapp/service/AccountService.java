package com.bankingapp.service;

import com.bankingapp.entity.Account;
import com.bankingapp.entity.User;
import com.bankingapp.exception.AccountNotFoundException;
import com.bankingapp.repository.AccountRepository;
import com.bankingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Account createAccount(Long userId, Account.AccountType accountType) {
        logger.info("Creating account for user: {}, type: {}", userId, accountType);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setAccountType(accountType);
        account.setIsActive(true);

        Account savedAccount = accountRepository.save(account);
        logger.info("Account created successfully: {}", savedAccount.getAccountNumber());
        
        return savedAccount;
    }

    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Account updateAccount(Long accountId, Account accountDetails) {
        Account account = getAccountById(accountId);
        
        if (accountDetails.getAccountType() != null) {
            account.setAccountType(accountDetails.getAccountType());
        }
        
        return accountRepository.save(account);
    }

    public void deleteAccount(Long accountId) {
        Account account = getAccountById(accountId);
        account.setIsActive(false);
        accountRepository.save(account);
        logger.info("Account deactivated: {}", account.getAccountNumber());
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        // Generate 10-digit account number
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        
        String accountNumber = sb.toString();
        
        // Check if account number already exists
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return generateAccountNumber(); // Recursive call to generate new number
        }
        
        return accountNumber;
    }
}
