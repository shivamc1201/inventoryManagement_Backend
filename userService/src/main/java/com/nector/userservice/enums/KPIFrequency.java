package com.nector.userservice.enums;

public enum KPIFrequency {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String label;

    KPIFrequency(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
