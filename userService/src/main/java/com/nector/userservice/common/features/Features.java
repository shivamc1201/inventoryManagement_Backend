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
    REPORTING_MANAGER("Reporting Manager", "/sales/salesperson-onboarding"),
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
    INVENTORY_TRANSACTIONS_SALE_INVOICE("Sale Invoice", "/inventory/transactions/sale-invoice"),

    // --- Transaction Sub-Items ---
    TRANSACTION_CASHBOOK("Cashbook", "/transaction/cashbook"),
    TRANSACTION_MASTER("Transaction Master", "/transaction/master"),

    // --- HR Sub-Items ---
    ADMIN_APPROVAL("Admin Approval", "/hr/admin-approval"),

    // --- Accounts Sub-Items ---
    ACCOUNTS_MASTER("Accounts Master", "/accounts/master"),
    ACCOUNTS_PAYMENT_REQUESTS("Payment Requests", "/accounts/payment-requests"),
    ACCOUNTS_PI_UPDATE("PI Update", "/accounts/pi-update"),

    // --- HR KRA/KPI ---
    HR_KRA_KPI("HR KRA / KPI", "/hr/kra-kpi"),

    // --- Support Sub-Items ---
    COMPLAINTS_MANAGEMENT("Complaints Management", "/complaint/complaints_management"),

    // --- Reports Section ---
    REPORTS("Reports", "/reports"),
    REPORT_MIS_DASHBOARD("MIS Dashboard", "/reports/mis-dashboard"),
    REPORT_INVENTORY("Inventory Reports", "/reports/inventory"),
    REPORT_STOCK_MOVEMENT("Stock Movement", "/reports/stock-movement"),
    REPORT_BATCH_MANAGEMENT("Batch Management", "/reports/batch-management"),
    REPORT_PRODUCTION("Production Reports", "/reports/production"),
    REPORT_SALES("Sales Reports", "/reports/sales"),
    REPORT_RECEIVABLES("Receivables & Collections", "/reports/receivables"),
    REPORT_SALES_ORDERS("Sales Orders", "/reports/sales-orders"),
    REPORT_DISPATCH("Dispatch & Delivery", "/reports/dispatch"),
    REPORT_INVENTORY_ISSUES("Inventory Issues", "/reports/inventory-issues"),
    REPORT_SCRAP("Scrap Management", "/reports/scrap");

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
