package com.nector.userservice.enums;

public enum OrderStatus {
    PENDING("pending"),
    APPROVED("approved"),
    COMPLETED("completed"),
    REJECTED("rejected"),
    PLACED("placed"),
    PAYMENT_APPROVED("payment_approved"),
    GDN_GENERATED("gdn_generated"),
    GDN_REJECTED("gdn_rejected"),
    ACTIVE("active"),
    DISMISSED("dismissed");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
