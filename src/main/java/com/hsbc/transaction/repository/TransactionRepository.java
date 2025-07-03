package com.hsbc.transaction.repository;

import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.model.TransactionType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {
    
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(idGenerator.getAndIncrement());
        }
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }
    
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(transactions.get(id));
    }
    
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }
    
    public List<Transaction> findAll(int page, int size) {
        List<Transaction> allTransactions = findAll();
        allTransactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());
        
        int start = page * size;
        int end = Math.min(start + size, allTransactions.size());
        
        if (start >= allTransactions.size()) {
            return Collections.emptyList();
        }
        
        return allTransactions.subList(start, end);
    }
    
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return transactions.values().stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByType(TransactionType type) {
        return transactions.values().stream()
                .filter(t -> t.getType() == type)
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return transactions.values().stream()
                .filter(t -> t.getAmount().compareTo(minAmount) >= 0 && t.getAmount().compareTo(maxAmount) <= 0)
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return transactions.values().stream()
                .filter(t -> t.getTransactionDate().isAfter(startDate) && t.getTransactionDate().isBefore(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByAccountNumberAndType(String accountNumber, TransactionType type) {
        return transactions.values().stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber) && t.getType() == type)
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }
    
    public boolean existsById(Long id) {
        return transactions.containsKey(id);
    }
    
    public void deleteById(Long id) {
        transactions.remove(id);
    }
    
    public void deleteAll() {
        transactions.clear();
        idGenerator.set(1);
    }
    
    public long count() {
        return transactions.size();
    }
    
    public Map<TransactionType, Long> countByType() {
        return transactions.values().stream()
                .collect(Collectors.groupingBy(
                    Transaction::getType,
                    Collectors.counting()
                ));
    }
    
    public Map<String, Long> countByAccountNumber() {
        return transactions.values().stream()
                .collect(Collectors.groupingBy(
                    Transaction::getAccountNumber,
                    Collectors.counting()
                ));
    }
    
    public BigDecimal getTotalAmount() {
        return transactions.values().stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalAmountByType(TransactionType type) {
        return transactions.values().stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalAmountByAccountNumber(String accountNumber) {
        return transactions.values().stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 