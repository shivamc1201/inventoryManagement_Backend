package com.nector.userservice.exception;

public class DuplicateLedgerReferenceException extends BusinessException {
    public DuplicateLedgerReferenceException(String message) {
        super(message);
    }
}
