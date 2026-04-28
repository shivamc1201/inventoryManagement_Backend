package com.nector.userservice.enums;

public enum Unit {
    KG("Kilogram"),
    LITER("Liter"),
    DOZEN("Dozen"),
    DOZENS("Dozens"),
    PIECES("Pieces");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
