package com.nector.userservice.ledger.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Response DTO for ledger summary information
 */
@Data
public class LedgerSummaryResponse {

    private Long accountId;
    private String accountNumber;
    private String accountName;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal netBalance;
    private BigDecimal closingBalance;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private Long transactionCount;
    private String status;

    // Computed fields
    public BigDecimal getAvailableCredit() {
        return closingBalance.add(creditLimit);
    }

    public boolean isOverCreditLimit() {
        return closingBalance.add(creditLimit).compareTo(BigDecimal.ZERO) < 0;
    }
}