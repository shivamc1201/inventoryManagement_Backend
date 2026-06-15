package com.nector.userservice.exception;

public class DuplicateLedgerNameException extends RuntimeException {
    public DuplicateLedgerNameException(String message) {
        super(message);
    }
}
