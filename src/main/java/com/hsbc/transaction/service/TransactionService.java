package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.PagedResponseDTO;
import com.hsbc.transaction.dto.TransactionRequestDTO;
import com.hsbc.transaction.dto.TransactionResponseDTO;
import com.hsbc.transaction.exception.DuplicateTransactionException;
import com.hsbc.transaction.exception.TransactionNotFoundException;
import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.model.TransactionType;
import com.hsbc.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    @Caching(evict = {
        @CacheEvict(value = "transactions", allEntries = true),
        @CacheEvict(value = "transactionStats", allEntries = true)
    })
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(requestDTO.getAccountNumber());
        transaction.setAmount(requestDTO.getAmount());
        transaction.setType(requestDTO.getType());
        transaction.setDescription(requestDTO.getDescription());
        transaction.setReference(requestDTO.getReference());
        transaction.setTransactionDate(LocalDateTime.now());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        return new TransactionResponseDTO(savedTransaction);
    }
    
    @Cacheable(value = "transactions", key = "#id")
    public TransactionResponseDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));
        return new TransactionResponseDTO(transaction);
    }
    
    @Cacheable(value = "transactions", key = "'all-' + #page + '-' + #size")
    public PagedResponseDTO<TransactionResponseDTO> getAllTransactions(int page, int size) {
        validatePagination(page, size);
        
        List<Transaction> transactions = transactionRepository.findAll(page, size);
        long totalElements = transactionRepository.count();
        
        List<TransactionResponseDTO> content = transactions.stream()
            .map(TransactionResponseDTO::new)
            .collect(Collectors.toList());
        
        return new PagedResponseDTO<>(content, page, size, totalElements);
    }
    
    @Cacheable(value = "transactions", key = "'account-' + #accountNumber")
    public List<TransactionResponseDTO> getTransactionsByAccountNumber(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return transactions.stream()
            .map(TransactionResponseDTO::new)
            .collect(Collectors.toList());
    }
    
    @Cacheable(value = "transactions", key = "'type-' + #type")
    public List<TransactionResponseDTO> getTransactionsByType(TransactionType type) {
        List<Transaction> transactions = transactionRepository.findByType(type);
        return transactions.stream()
            .map(TransactionResponseDTO::new)
            .collect(Collectors.toList());
    }
    
    @Cacheable(value = "transactions", key = "'amount-' + #minAmount + '-' + #maxAmount")
    public List<TransactionResponseDTO> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Minimum amount cannot be greater than maximum amount");
        }
        
        List<Transaction> transactions = transactionRepository.findByAmountBetween(minAmount, maxAmount);
        return transactions.stream()
            .map(TransactionResponseDTO::new)
            .collect(Collectors.toList());
    }
    
    @Cacheable(value = "transactions", key = "'date-' + #startDate + '-' + #endDate")
    public List<TransactionResponseDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        List<Transaction> transactions = transactionRepository.findByDateBetween(startDate, endDate);
        return transactions.stream()
            .map(TransactionResponseDTO::new)
            .collect(Collectors.toList());
    }
    
    @Caching(evict = {
        @CacheEvict(value = "transactions", allEntries = true),
        @CacheEvict(value = "transactionStats", allEntries = true)
    })
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO requestDTO) {
        Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));
        
        existingTransaction.setAccountNumber(requestDTO.getAccountNumber());
        existingTransaction.setAmount(requestDTO.getAmount());
        existingTransaction.setType(requestDTO.getType());
        existingTransaction.setDescription(requestDTO.getDescription());
        existingTransaction.setReference(requestDTO.getReference());
        
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        return new TransactionResponseDTO(updatedTransaction);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "transactions", allEntries = true),
        @CacheEvict(value = "transactionStats", allEntries = true)
    })
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException(id);
        }
        transactionRepository.deleteById(id);
    }
    
    @Cacheable(value = "transactionStats", key = "'count'")
    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }
    
    @Cacheable(value = "transactionStats", key = "'countByType'")
    public Map<TransactionType, Long> getTransactionCountByType() {
        return transactionRepository.countByType();
    }
    
    @Cacheable(value = "transactionStats", key = "'countByAccount'")
    public Map<String, Long> getTransactionCountByAccount() {
        return transactionRepository.countByAccountNumber();
    }
    
    @Cacheable(value = "transactionStats", key = "'totalAmount'")
    public BigDecimal getTotalTransactionAmount() {
        return transactionRepository.getTotalAmount();
    }
    
    @Cacheable(value = "transactionStats", key = "'totalAmountByType-' + #type")
    public BigDecimal getTotalAmountByType(TransactionType type) {
        return transactionRepository.getTotalAmountByType(type);
    }
    
    @Cacheable(value = "transactionStats", key = "'totalAmountByAccount-' + #accountNumber")
    public BigDecimal getTotalAmountByAccount(String accountNumber) {
        return transactionRepository.getTotalAmountByAccountNumber(accountNumber);
    }
    
    public boolean transactionExists(Long id) {
        return transactionRepository.existsById(id);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "transactions", allEntries = true),
        @CacheEvict(value = "transactionStats", allEntries = true)
    })
    public void deleteAllTransactions() {
        transactionRepository.deleteAll();
    }
    
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
} 