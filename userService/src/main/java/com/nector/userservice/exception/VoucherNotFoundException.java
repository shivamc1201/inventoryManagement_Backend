package com.nector.userservice.exception;

public class VoucherNotFoundException extends RuntimeException {
    public VoucherNotFoundException(String message) {
        super(message);
    }
}
