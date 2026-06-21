package com.nector.userservice.exception;

public class ActiveOrderExistsException extends RuntimeException {
    public ActiveOrderExistsException(String message) {
        super(message);
    }
}
