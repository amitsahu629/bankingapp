package com.bankingapp.integration;

import com.bankingapp.entity.Account;
import com.bankingapp.entity.User;
import com.bankingapp.repository.AccountRepository;
import com.bankingapp.repository.UserRepository;
import com.bankingapp.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = new User("testuser", "test@example.com", "password", "John", "Doe");
        user = userRepository.save(user);

        // Create test account
        testAccount = new Account("1234567890", user, Account.AccountType.CHECKING);
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount = accountRepository.save(testAccount);

        // Generate JWT token
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);
        jwtToken = jwtTokenProvider.generateToken(auth);
    }

    @Test
    public void testDeposit_Success() throws Exception {
        Map<String, Object> depositRequest = new HashMap<>();
        depositRequest.put("accountId", testAccount.getId());
        depositRequest.put("amount", 500.00);
        depositRequest.put("description", "Test deposit");

        mockMvc.perform(post("/api/transactions/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testWithdraw_InsufficientFunds() throws Exception {
        Map<String, Object> withdrawRequest = new HashMap<>();
        withdrawRequest.put("accountId", testAccount.getId());
        withdrawRequest.put("amount", 2000.00); // More than available balance
        withdrawRequest.put("description", "Test withdrawal");

        mockMvc.perform(post("/api/transactions/withdraw")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds in account"));
    }
}
