package com.nector.userservice.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public CartNotFoundException(Long userId) {
        super("Active cart not found for user: " + userId);
    }
}