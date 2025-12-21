package com.nector.userservice.common;


public enum RoleType {

    SUPER_ADMIN("Administration"),

    ADMIN("Administration"),

    // --- Department Managers/Directors ---
    BUSINESS_DEV_MGR("BusinessDevelopment"),
    PLANT_MGR("Plant"),
    HR_MGR("HR"),
    LOGISTICS_MGR("Logistics"),

    // --- Account Department Roles ---
    ACCOUNT_MGR("Accounts"),
    ACCOUNT_OFFICER("Accounts"),
    ACCOUNT_EXECUTIVE("Accounts"),

    // --- Sales Department Roles (Granular) ---
    NATIONAL_SALES_MGR("Sales"),
    STATE_SALES_MGR("Sales"),
    ZONAL_SALES_MGR("Sales"),
    REGIONAL_SALES_MGR("Sales"),
    AREA_SALES_MGR("Sales"),
    SALES_OFFICER("Sales"),
    SALES_EXECUTIVE("Sales"),

    // --- Logistics Department Roles ---
    LOGISTICS_OFFICER("Logistics"),

    // --- HR Department Roles ---
    HR_EXECUTIVE("HR"),

    // --- Plant Department Roles ---
    PLANT_OFFICER("Plant"),
    PLANT_EXECUTIVE("Plant");

    // Private field to hold the department name
    private final String department;

    RoleType(String department) {
        this.department = department;
    }

    // Public getter method
    public String getDepartment() {
        return department;
    }

}