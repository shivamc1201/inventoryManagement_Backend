package com.nector.userservice.ledger.enums;

/**
 * Enumeration for ledger account status
 */
public enum AccountStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    FROZEN("Frozen"),
    CLOSED("Closed");

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}