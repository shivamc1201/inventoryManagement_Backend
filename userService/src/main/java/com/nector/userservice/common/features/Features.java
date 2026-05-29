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
    SALES("Sales", "/sales"), // Parent for all Sales sub-features
    REPORTS("Reports", "/reports"),
    COMPLAINT("Complaint", "/complaint"),
    PRODUCTS("Products", "/products"), // Parent for all Product sub-features
    ORDER_DETAILS("OrderDetails", "/order-details"),
    LOGISTIC("LOGISTIC", "/logistic" ),
    USER_RIGHTS("User Rights", "/user-rights"),
    DISPATCH("Dispatch", "/dispatch"),


    // --- Product Sub-Items ---
    PRODUCTS_FINISHED_PRODUCTS("Finished Products", "/products/finished-products"),
    PRODUCTS_RAW_MATERIALS("Raw Materials", "/products/raw-materials"),
    PRODUCTS_MACHINE_PARTS("Machine Parts", "/products/machine-parts"),

    // --- Inventory Sub-Items ---
    // Masters
    INVENTORY_MASTERS("Master Inventory", "/inventory/masters"),

    // Inward / Outward
    INVENTORY_INWARD("Inward Inventory", "/inventory/inward"),
    INVENTORY_OUTWARD("Outward Inventory", "/inventory/outward"),

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
