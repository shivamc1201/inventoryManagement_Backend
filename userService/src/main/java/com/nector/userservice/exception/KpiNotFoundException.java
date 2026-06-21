package com.nector.userservice.exception;

public class KpiNotFoundException extends KpiException {
    
    public KpiNotFoundException(String message) {
        super(message);
    }
    
    public KpiNotFoundException(Long kpiId) {
        super("KPI not found with ID: " + kpiId);
    }
}
