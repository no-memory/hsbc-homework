package com.hsbc.transaction.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    
    @NotNull(message = "Transaction ID cannot be null")
    private Long id;
    
    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "^[0-9]{10,16}$", message = "Account number must be 10-16 digits")
    private String accountNumber;
    
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;
    
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Transaction date cannot be null")
    private LocalDateTime transactionDate;
    
    private String reference;
    
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }
    
    public Transaction(Long id, String accountNumber, BigDecimal amount, TransactionType type, String description) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }
    
    public Transaction(Long id, String accountNumber, BigDecimal amount, TransactionType type, String description, String reference) {
        this(id, accountNumber, amount, type, description);
        this.reference = reference;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(accountNumber, that.accountNumber) &&
               Objects.equals(amount, that.amount) &&
               type == that.type &&
               Objects.equals(description, that.description) &&
               Objects.equals(transactionDate, that.transactionDate) &&
               Objects.equals(reference, that.reference);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber, amount, type, description, transactionDate, reference);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
               "id=" + id +
               ", accountNumber='" + accountNumber + '\'' +
               ", amount=" + amount +
               ", type=" + type +
               ", description='" + description + '\'' +
               ", transactionDate=" + transactionDate +
               ", reference='" + reference + '\'' +
               '}';
    }
} 