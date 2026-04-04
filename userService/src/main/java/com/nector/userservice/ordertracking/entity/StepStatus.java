package com.nector.userservice.ordertracking.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StepStatus {
    COMPLETED("completed"),
    PENDING("pending"),
    CANCELLED("cancelled"),
    IN_PROGRESS("in-progress");

    private final String value;

    StepStatus(String value) { 
        this.value = value; 
    }

    @JsonValue
    public String getValue() { 
        return value; 
    }

    public static StepStatus from(String s) {
        for (StepStatus v : values()) {
            if (v.name().equalsIgnoreCase(s) || v.value.equalsIgnoreCase(s)) return v;
        }
        throw new IllegalArgumentException("Unknown step status: " + s);
    }
}
