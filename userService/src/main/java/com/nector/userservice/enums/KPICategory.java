package com.nector.userservice.enums;

public enum KPICategory {
    KRA("Key Result Area"),
    KPI("Key Performance Indicator");

    private final String label;

    KPICategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
