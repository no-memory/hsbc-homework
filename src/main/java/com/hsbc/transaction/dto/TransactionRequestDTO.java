package com.hsbc.transaction.dto;

import com.hsbc.transaction.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransactionRequestDTO {
    
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
    
    private String reference;
    
    public TransactionRequestDTO() {}
    
    public TransactionRequestDTO(String accountNumber, BigDecimal amount, TransactionType type, String description) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
    
    public TransactionRequestDTO(String accountNumber, BigDecimal amount, TransactionType type, String description, String reference) {
        this(accountNumber, amount, type, description);
        this.reference = reference;
    }
    
    // Getters
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
    
    public String getReference() {
        return reference;
    }
    
    // Setters
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
    
    public void setReference(String reference) {
        this.reference = reference;
    }
} 