package com.nector.userservice.enums;

public enum SalesRole {
    NATIONAL_SALES_MGR("National Sales Manager", "NSM", "globe-outline"),
    STATE_SALES_MGR("State Sales Manager", "SSM", "ribbon-outline"),
    ZONAL_SALES_MGR("Zonal Sales Manager", "ZSM", "map-outline"),
    REGIONAL_SALES_MGR("Regional Sales Manager", "RSM", "location-outline"),
    AREA_SALES_MGR("Area Sales Manager", "ASM", "business-outline"),
    SALES_OFFICER("Sales Officer", "SO", "briefcase-outline"),
    SALES_EXECUTIVE("Sales Executive", "SE", "person-outline");

    private final String label;
    private final String shortLabel;
    private final String icon;

    SalesRole(String label, String shortLabel, String icon) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getIcon() {
        return icon;
    }
}
