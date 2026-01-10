package com.nector.userservice.exception;

public class MachinePartNotFoundException extends RuntimeException {
    public MachinePartNotFoundException(Long id) {
        super("Machine part not found with ID: " + id);
    }
    
    public MachinePartNotFoundException(String partNumber) {
        super("Machine part not found with part number: " + partNumber);
    }
}