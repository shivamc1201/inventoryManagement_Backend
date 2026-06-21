package com.nector.userservice.ledger.enums;

/**
 * Enumeration for ledger transaction types
 * Defines all supported transaction types in the financial ledger system
 */
public enum TransactionType {
    OPENING_BALANCE("Opening Balance", false, true),
    SALE("Sale Transaction", false, true),
    PURCHASE_RETURN("Purchase Return", true, false),
    PAYMENT_RECEIVED("Payment Received", true, false),
    PAYMENT_REVERSAL("Payment Reversal", false, true),
    CREDIT_NOTE("Credit Note", true, false),
    DEBIT_NOTE("Debit Note", false, true),
    ADJUSTMENT("Account Adjustment", null, null); // Can be either debit or credit

    private final String displayName;
    private final Boolean isDebit;
    private final Boolean isCredit;

    TransactionType(String displayName, Boolean isDebit, Boolean isCredit) {
        this.displayName = displayName;
        this.isDebit = isDebit;
        this.isCredit = isCredit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Boolean isDebit() {
        return isDebit;
    }

    public Boolean isCredit() {
        return isCredit;
    }

    public boolean isAdjustment() {
        return this == ADJUSTMENT;
    }
}