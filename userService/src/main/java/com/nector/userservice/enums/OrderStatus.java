package com.nector.userservice.enums;

public enum OrderStatus {
    PENDING("pending"),
    APPROVED("approved"),
    COMPLETED("completed"),
    REJECTED("rejected");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
