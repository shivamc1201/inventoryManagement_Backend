package com.nector.userservice.exception;

public class KpiException extends BusinessException {
    
    public KpiException(String message) {
        super(message);
    }
    
    public KpiException(String message, Throwable cause) {
        super(message, cause);
    }
}
