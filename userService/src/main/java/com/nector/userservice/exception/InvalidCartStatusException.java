package com.nector.userservice.exception;

public class InvalidCartStatusException extends RuntimeException {
    public InvalidCartStatusException(String message) {
        super(message);
    }
}