package com.hsbc.transaction.dto;

import com.hsbc.transaction.model.Transaction;
import com.hsbc.transaction.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    
    private Long id;
    private String accountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDateTime transactionDate;
    private String reference;
    
    public TransactionResponseDTO() {}
    
    public TransactionResponseDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.accountNumber = transaction.getAccountNumber();
        this.amount = transaction.getAmount();
        this.type = transaction.getType();
        this.description = transaction.getDescription();
        this.transactionDate = transaction.getTransactionDate();
        this.reference = transaction.getReference();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public String getReference() {
        return reference;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
} 