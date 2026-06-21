package com.nector.userservice.exception;

public class ReservedLedgerNameException extends RuntimeException {
    public ReservedLedgerNameException(String message) {
        super(message);
    }
}
