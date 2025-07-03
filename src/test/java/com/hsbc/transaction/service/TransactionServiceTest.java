package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.PagedResponseDTO;
import com.hsbc.transaction.dto.TransactionRequestDTO;
import com.hsbc.transaction.dto.TransactionResponseDTO;
import com.hsbc.transaction.exception.TransactionNotFoundException;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.model.TransactionType;
import com.hsbc.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private TransactionRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction(
            1L,
            "1234567890",
            new BigDecimal("100.50"),
            TransactionType.CREDIT,
            "Test transaction"
        );

        testRequestDTO = new TransactionRequestDTO(
            "1234567890",
            new BigDecimal("100.50"),
            TransactionType.CREDIT,
            "Test transaction"
        );
    }

    @Test
    void createTransaction_ShouldReturnTransactionResponseDTO() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponseDTO result = transactionService.createTransaction(testRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAccountNumber(), result.getAccountNumber());
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(testTransaction.getType(), result.getType());
        assertEquals(testTransaction.getDescription(), result.getDescription());
        
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_ShouldReturnTransactionResponseDTO() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        TransactionResponseDTO result = transactionService.getTransactionById(transactionId);

        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAccountNumber(), result.getAccountNumber());
        
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void getTransactionById_ShouldThrowTransactionNotFoundException() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionNotFoundException.class, 
            () -> transactionService.getTransactionById(transactionId));
        
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void getAllTransactions_ShouldReturnPagedResponse() {
        // Given
        int page = 0;
        int size = 10;
        List<Transaction> transactions = Arrays.asList(testTransaction);
        long totalElements = 1L;
        
        when(transactionRepository.findAll(page, size)).thenReturn(transactions);
        when(transactionRepository.count()).thenReturn(totalElements);

        // When
        PagedResponseDTO<TransactionResponseDTO> result = transactionService.getAllTransactions(page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(totalElements, result.getTotalElements());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        
        verify(transactionRepository).findAll(page, size);
        verify(transactionRepository).count();
    }

    @Test
    void getAllTransactions_ShouldThrowIllegalArgumentException_WhenPageIsNegative() {
        // Given
        int page = -1;
        int size = 10;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.getAllTransactions(page, size));
    }

    @Test
    void getAllTransactions_ShouldThrowIllegalArgumentException_WhenSizeIsZero() {
        // Given
        int page = 0;
        int size = 0;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.getAllTransactions(page, size));
    }

    @Test
    void getAllTransactions_ShouldThrowIllegalArgumentException_WhenSizeExceedsLimit() {
        // Given
        int page = 0;
        int size = 101;

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.getAllTransactions(page, size));
    }

    @Test
    void getTransactionsByAccountNumber_ShouldReturnTransactionList() {
        // Given
        String accountNumber = "1234567890";
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(transactionRepository.findByAccountNumber(accountNumber)).thenReturn(transactions);

        // When
        List<TransactionResponseDTO> result = transactionService.getTransactionsByAccountNumber(accountNumber);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        
        verify(transactionRepository).findByAccountNumber(accountNumber);
    }

    @Test
    void getTransactionsByType_ShouldReturnTransactionList() {
        // Given
        TransactionType type = TransactionType.CREDIT;
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(transactionRepository.findByType(type)).thenReturn(transactions);

        // When
        List<TransactionResponseDTO> result = transactionService.getTransactionsByType(type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        
        verify(transactionRepository).findByType(type);
    }

    @Test
    void getTransactionsByAmountRange_ShouldReturnTransactionList() {
        // Given
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("150.00");
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(transactionRepository.findByAmountBetween(minAmount, maxAmount)).thenReturn(transactions);

        // When
        List<TransactionResponseDTO> result = transactionService.getTransactionsByAmountRange(minAmount, maxAmount);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        
        verify(transactionRepository).findByAmountBetween(minAmount, maxAmount);
    }

    @Test
    void getTransactionsByAmountRange_ShouldThrowIllegalArgumentException_WhenMinAmountGreaterThanMaxAmount() {
        // Given
        BigDecimal minAmount = new BigDecimal("150.00");
        BigDecimal maxAmount = new BigDecimal("50.00");

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.getTransactionsByAmountRange(minAmount, maxAmount));
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnTransactionList() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(transactionRepository.findByDateBetween(startDate, endDate)).thenReturn(transactions);

        // When
        List<TransactionResponseDTO> result = transactionService.getTransactionsByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
        
        verify(transactionRepository).findByDateBetween(startDate, endDate);
    }

    @Test
    void getTransactionsByDateRange_ShouldThrowIllegalArgumentException_WhenStartDateAfterEndDate() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    @Test
    void updateTransaction_ShouldReturnUpdatedTransaction() {
        // Given
        Long transactionId = 1L;
        TransactionRequestDTO updateDTO = new TransactionRequestDTO(
            "0987654321",
            new BigDecimal("200.00"),
            TransactionType.DEBIT,
            "Updated transaction"
        );
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponseDTO result = transactionService.updateTransaction(transactionId, updateDTO);

        // Then
        assertNotNull(result);
        
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_ShouldThrowTransactionNotFoundException() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionNotFoundException.class, 
            () -> transactionService.updateTransaction(transactionId, testRequestDTO));
        
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_ShouldDeleteTransaction() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.existsById(transactionId)).thenReturn(true);

        // When
        transactionService.deleteTransaction(transactionId);

        // Then
        verify(transactionRepository).existsById(transactionId);
        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void deleteTransaction_ShouldThrowTransactionNotFoundException() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        // When & Then
        assertThrows(TransactionNotFoundException.class, 
            () -> transactionService.deleteTransaction(transactionId));
        
        verify(transactionRepository).existsById(transactionId);
        verify(transactionRepository, never()).deleteById(transactionId);
    }

    @Test
    void getTotalTransactionCount_ShouldReturnCount() {
        // Given
        long expectedCount = 5L;
        when(transactionRepository.count()).thenReturn(expectedCount);

        // When
        long result = transactionService.getTotalTransactionCount();

        // Then
        assertEquals(expectedCount, result);
        verify(transactionRepository).count();
    }

    @Test
    void getTotalTransactionAmount_ShouldReturnTotalAmount() {
        // Given
        BigDecimal expectedAmount = new BigDecimal("500.00");
        when(transactionRepository.getTotalAmount()).thenReturn(expectedAmount);

        // When
        BigDecimal result = transactionService.getTotalTransactionAmount();

        // Then
        assertEquals(expectedAmount, result);
        verify(transactionRepository).getTotalAmount();
    }

    @Test
    void getTotalAmountByType_ShouldReturnTotalAmountForType() {
        // Given
        TransactionType type = TransactionType.CREDIT;
        BigDecimal expectedAmount = new BigDecimal("300.00");
        when(transactionRepository.getTotalAmountByType(type)).thenReturn(expectedAmount);

        // When
        BigDecimal result = transactionService.getTotalAmountByType(type);

        // Then
        assertEquals(expectedAmount, result);
        verify(transactionRepository).getTotalAmountByType(type);
    }

    @Test
    void getTotalAmountByAccount_ShouldReturnTotalAmountForAccount() {
        // Given
        String accountNumber = "1234567890";
        BigDecimal expectedAmount = new BigDecimal("400.00");
        when(transactionRepository.getTotalAmountByAccountNumber(accountNumber)).thenReturn(expectedAmount);

        // When
        BigDecimal result = transactionService.getTotalAmountByAccount(accountNumber);

        // Then
        assertEquals(expectedAmount, result);
        verify(transactionRepository).getTotalAmountByAccountNumber(accountNumber);
    }

    @Test
    void transactionExists_ShouldReturnTrueWhenExists() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.existsById(transactionId)).thenReturn(true);

        // When
        boolean result = transactionService.transactionExists(transactionId);

        // Then
        assertTrue(result);
        verify(transactionRepository).existsById(transactionId);
    }

    @Test
    void transactionExists_ShouldReturnFalseWhenNotExists() {
        // Given
        Long transactionId = 1L;
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        // When
        boolean result = transactionService.transactionExists(transactionId);

        // Then
        assertFalse(result);
        verify(transactionRepository).existsById(transactionId);
    }

    @Test
    void deleteAllTransactions_ShouldDeleteAll() {
        // When
        transactionService.deleteAllTransactions();

        // Then
        verify(transactionRepository).deleteAll();
    }
} 