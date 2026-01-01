package com.nector.userservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String sku, int requested, int available) {
        super(String.format("Insufficient stock for item %s. Requested: %d, Available: %d", sku, requested, available));
    }
}