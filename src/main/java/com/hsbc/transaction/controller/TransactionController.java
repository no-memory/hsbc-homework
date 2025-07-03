package com.hsbc.transaction.controller;

import com.hsbc.transaction.dto.PagedResponseDTO;
import com.hsbc.transaction.dto.TransactionRequestDTO;
import com.hsbc.transaction.dto.TransactionResponseDTO;
import com.hsbc.transaction.model.TransactionType;
import com.hsbc.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing banking transactions")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Creates a new transaction with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Duplicate transaction")
    })
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {
        TransactionResponseDTO responseDTO = transactionService.createTransaction(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a transaction by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {
        TransactionResponseDTO responseDTO = transactionService.getTransactionById(id);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves all transactions with pagination support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<PagedResponseDTO<TransactionResponseDTO>> getAllTransactions(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PagedResponseDTO<TransactionResponseDTO> responseDTO = transactionService.getAllTransactions(page, size);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transactions by account number", description = "Retrieves all transactions for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid account number format")
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccountNumber(
            @Parameter(description = "Account number (10-16 digits)", required = true)
            @PathVariable String accountNumber) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Retrieves all transactions of a specific type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction type")
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByType(
            @Parameter(description = "Transaction type", required = true)
            @PathVariable TransactionType type) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByType(type);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/amount-range")
    @Operation(summary = "Get transactions by amount range", description = "Retrieves transactions within a specified amount range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid amount range")
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAmountRange(
            @Parameter(description = "Minimum amount", required = true)
            @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount", required = true)
            @RequestParam BigDecimal maxAmount) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates an existing transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO requestDTO) {
        TransactionResponseDTO responseDTO = transactionService.updateTransaction(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Deletes a transaction by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/statistics/count")
    @Operation(summary = "Get transaction count", description = "Retrieves the total number of transactions")
    @ApiResponse(responseCode = "200", description = "Transaction count retrieved successfully")
    public ResponseEntity<Long> getTotalTransactionCount() {
        long count = transactionService.getTotalTransactionCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/statistics/count-by-type")
    @Operation(summary = "Get transaction count by type", description = "Retrieves transaction counts grouped by type")
    @ApiResponse(responseCode = "200", description = "Transaction counts by type retrieved successfully")
    public ResponseEntity<Map<TransactionType, Long>> getTransactionCountByType() {
        Map<TransactionType, Long> counts = transactionService.getTransactionCountByType();
        return ResponseEntity.ok(counts);
    }
    
    @GetMapping("/statistics/count-by-account")
    @Operation(summary = "Get transaction count by account", description = "Retrieves transaction counts grouped by account number")
    @ApiResponse(responseCode = "200", description = "Transaction counts by account retrieved successfully")
    public ResponseEntity<Map<String, Long>> getTransactionCountByAccount() {
        Map<String, Long> counts = transactionService.getTransactionCountByAccount();
        return ResponseEntity.ok(counts);
    }
    
    @GetMapping("/statistics/total-amount")
    @Operation(summary = "Get total transaction amount", description = "Retrieves the total amount of all transactions")
    @ApiResponse(responseCode = "200", description = "Total transaction amount retrieved successfully")
    public ResponseEntity<BigDecimal> getTotalTransactionAmount() {
        BigDecimal totalAmount = transactionService.getTotalTransactionAmount();
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/statistics/total-amount-by-type/{type}")
    @Operation(summary = "Get total amount by type", description = "Retrieves the total amount for a specific transaction type")
    @ApiResponse(responseCode = "200", description = "Total amount by type retrieved successfully")
    public ResponseEntity<BigDecimal> getTotalAmountByType(
            @Parameter(description = "Transaction type", required = true)
            @PathVariable TransactionType type) {
        BigDecimal totalAmount = transactionService.getTotalAmountByType(type);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/statistics/total-amount-by-account/{accountNumber}")
    @Operation(summary = "Get total amount by account", description = "Retrieves the total amount for a specific account")
    @ApiResponse(responseCode = "200", description = "Total amount by account retrieved successfully")
    public ResponseEntity<BigDecimal> getTotalAmountByAccount(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        BigDecimal totalAmount = transactionService.getTotalAmountByAccount(accountNumber);
        return ResponseEntity.ok(totalAmount);
    }
    
    @DeleteMapping
    @Operation(summary = "Delete all transactions", description = "Deletes all transactions (for testing purposes)")
    @ApiResponse(responseCode = "204", description = "All transactions deleted successfully")
    public ResponseEntity<Void> deleteAllTransactions() {
        transactionService.deleteAllTransactions();
        return ResponseEntity.noContent().build();
    }
} 