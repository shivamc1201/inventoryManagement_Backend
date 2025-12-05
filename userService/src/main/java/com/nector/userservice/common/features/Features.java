package com.nector.userservice.common.features;

import lombok.Getter;


@Getter
public enum Features {

    // --- Top-Level Menu Items ---
    DASHBOARD("Dashboard", "/dashboard"),
    ACCOUNTS("Accounts", "/accounts"),
    HR("HR", "/hr"),
    DISTRIBUTOR("Distributor", "/distributor"),
    INVENTORY("Inventory", "/inventory"), // Parent for all Inventory sub-features
    REPORTS("Reports", "/reports"),
    USER_RIGHTS("User Rights", "/user-rights"),

    // --- Inventory Sub-Items ---
    // Masters
    INVENTORY_MASTERS("Masters", "/inventory/masters"),

    // Transactions
    INVENTORY_TRANSACTIONS("Transactions", "/inventory/transactions"), // Parent for all Inventory Transactions
    INVENTORY_TRANSACTIONS_PROFORMA_INVOICE("Proforma Invoice", "/inventory/transactions/proforma-invoice"),
    INVENTORY_TRANSACTIONS_PO_DEPOSIT_RECEIPTS_LIST("PO Deposit Receipts List", "/inventory/transactions/po-deposit-receipts-list"),
    INVENTORY_TRANSACTIONS_PO_LIST("PO List", "/inventory/transactions/po-list"),
    INVENTORY_TRANSACTIONS_OUTWARD_CHALLAN("Outward Challan", "/inventory/transactions/outward-challan"),
    INVENTORY_TRANSACTIONS_SALE_INVOICE("Sale Invoice", "/inventory/transactions/sale-invoice");

    // Getters
    // --- Enum Attributes ---
    private final String displayName; // User-friendly name for UI
    private final String path;        // Corresponding URL path (optional, but good for UI/routing)

    // Constructor
    Features(String displayName, String path) {
        this.displayName = displayName;
        this.path = path;
    }

}
