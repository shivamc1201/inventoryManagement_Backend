package com.nector.userservice.exception;

public class TenantAccessViolationException extends BusinessException {
    public TenantAccessViolationException(String message) {
        super(message);
    }
}
