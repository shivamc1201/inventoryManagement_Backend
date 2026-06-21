package com.nector.userservice.enums;

public enum KPIMeasurementUnit {
    AMOUNT("Amount in Currency"),
    COUNT("Count/Number"),
    PERCENTAGE("Percentage"),
    VISITS("Number of Visits");

    private final String label;

    KPIMeasurementUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
