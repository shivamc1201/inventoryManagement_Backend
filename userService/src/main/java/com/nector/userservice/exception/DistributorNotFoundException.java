package com.nector.userservice.exception;

public class DistributorNotFoundException extends RuntimeException {
    public DistributorNotFoundException(String message) {
        super(message);
    }
    
    public DistributorNotFoundException(Long distributorId) {
        super("Distributor with ID " + distributorId + " not found");
    }
}
