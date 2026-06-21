package com.nector.userservice.ledger.exception;

/**
 * Exception thrown when ledger account is not found
 */
public class LedgerAccountNotFoundException extends RuntimeException {
    
    public LedgerAccountNotFoundException(String message) {
        super(message);
    }
    
    public LedgerAccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when transaction validation fails
 */
class LedgerTransactionValidationException extends RuntimeException {
    
    public LedgerTransactionValidationException(String message) {
        super(message);
    }
    
    public LedgerTransactionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when insufficient balance for debit transaction
 */
class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}