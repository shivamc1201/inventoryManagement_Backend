package com.nector.userservice.exception;

public class InvalidLedgerGroupException extends RuntimeException {
    public InvalidLedgerGroupException(String message) {
        super(message);
    }
}
