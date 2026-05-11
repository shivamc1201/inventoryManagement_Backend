package com.nector.userservice.enums;

public enum KPIStatus {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    EXPIRED("Expired");

    private final String label;

    KPIStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
