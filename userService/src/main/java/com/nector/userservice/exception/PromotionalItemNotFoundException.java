package com.nector.userservice.exception;

public class PromotionalItemNotFoundException extends RuntimeException {
    public PromotionalItemNotFoundException(Long id) {
        super("Promotional item not found with ID: " + id);
    }
    
    public PromotionalItemNotFoundException(String itemCode) {
        super("Promotional item not found with item code: " + itemCode);
    }
}
