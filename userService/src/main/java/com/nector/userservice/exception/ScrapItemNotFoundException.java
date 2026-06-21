package com.nector.userservice.exception;

public class ScrapItemNotFoundException extends RuntimeException {
    public ScrapItemNotFoundException(Long id) {
        super("Scrap item not found with ID: " + id);
    }
    
    public ScrapItemNotFoundException(String itemCode) {
        super("Scrap item not found with item code: " + itemCode);
    }
}
