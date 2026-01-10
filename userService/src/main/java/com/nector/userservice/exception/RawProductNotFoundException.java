package com.nector.userservice.exception;

public class RawProductNotFoundException extends RuntimeException {
    public RawProductNotFoundException(Long id) {
        super("Raw product not found with ID: " + id);
    }
    
    public RawProductNotFoundException(String materialCode) {
        super("Raw product not found with material code: " + materialCode);
    }
}