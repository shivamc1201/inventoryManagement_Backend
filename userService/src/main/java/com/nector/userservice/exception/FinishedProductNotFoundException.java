package com.nector.userservice.exception;

public class FinishedProductNotFoundException extends RuntimeException {
    public FinishedProductNotFoundException(Long id) {
        super("Finished product not found with ID: " + id);
    }
    
    public FinishedProductNotFoundException(String sku) {
        super("Finished product not found with SKU: " + sku);
    }
}