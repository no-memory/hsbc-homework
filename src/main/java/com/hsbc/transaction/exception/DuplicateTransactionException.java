package com.hsbc.transaction.exception;

public class DuplicateTransactionException extends RuntimeException {
    
    public DuplicateTransactionException(String message) {
        super(message);
    }
    
    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DuplicateTransactionException(Long id) {
        super("Transaction with ID " + id + " already exists");
    }
} 