package com.nector.userservice.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String sku, int requested, int available) {
        super(String.format("Insufficient stock for item %s. Requested: %d, Available: %d", sku, requested, available));
    }

    public InsufficientStockException(String sku, BigDecimal requested, BigDecimal available) {
        super(String.format("Insufficient stock for item %s. Requested: %s, Available: %s", sku, requested, available));
    }
}