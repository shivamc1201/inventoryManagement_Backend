package com.nector.userservice.enums;

public enum Unit {
    KG("Kilogram"),
    LITER("Liter"),
    LITRE("Litre"),
    LITRES("Litres"),
    DOZEN("Dozen"),
    DOZENS("Dozens"),
    PIECE("Piece"),
    PIECES("Pieces");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
