package com.hsbc.transaction.model;


public enum TransactionType {
    CREDIT("CREDIT"),
    DEBIT("DEBIT"),
    TRANSFER("TRANSFER"),
    PAYMENT("PAYMENT"),
    DEPOSIT("DEPOSIT"),
    WITHDRAWAL("WITHDRAWAL"),
    FEE("FEE"),
    INTEREST("INTEREST"),
    REFUND("REFUND"),
    ADJUSTMENT("ADJUSTMENT");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 