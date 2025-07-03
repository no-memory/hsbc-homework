package com.hsbc.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction.dto.TransactionRequestDTO;
import com.hsbc.transaction.model.TransactionType;
import com.hsbc.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.cache.type=none",
    "logging.level.com.hsbc.transaction=DEBUG"
})
class TransactionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TransactionRepository transactionRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        
        // Clear repository before each test
        transactionRepository.deleteAll();
    }

    @Test
    void createTransaction_ShouldReturnCreated() throws Exception {
        // Given
        TransactionRequestDTO requestDTO = new TransactionRequestDTO(
            "1234567890",
            new BigDecimal("100.50"),
            TransactionType.CREDIT,
            "Test transaction"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.description").value("Test transaction"))
                .andExpect(jsonPath("$.transactionDate").exists());
    }

    @Test
    void createTransaction_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequestDTO requestDTO = new TransactionRequestDTO(
            "123", // Invalid account number
            new BigDecimal("-100.50"), // Invalid amount
            TransactionType.CREDIT,
            "" // Empty description
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void deleteAllTransactions_ShouldDeleteAllTransactions() throws Exception {
        // Given
        TransactionRequestDTO requestDTO = new TransactionRequestDTO(
            "1234567890",
            new BigDecimal("100.50"),
            TransactionType.CREDIT,
            "Test transaction"
        );

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(delete("/api/v1/transactions"))
                .andExpect(status().isNoContent());

        // Verify all transactions are deleted
        mockMvc.perform(get("/api/v1/transactions/statistics/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
} 