package com.hsbc.transaction.exception;

public class TransactionNotFoundException extends RuntimeException {
    
    public TransactionNotFoundException(String message) {
        super(message);
    }
    
    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TransactionNotFoundException(Long id) {
        super("Transaction with ID " + id + " not found");
    }
} 