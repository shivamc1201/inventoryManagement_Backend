package com.nector.userservice.ledger.dto;

import com.nector.userservice.ledger.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for ledger transaction
 */
@Data
public class TransactionResponse {

    private Long id;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private String referenceNumber;
    private String description;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal runningBalance;
    private String categoryName;
    private String notes;
    private String externalReference;
    private Boolean isReversal;
    private Long reversedTransactionId;
    private LocalDateTime createdAt;
    private String createdBy;

    // Computed fields
    public BigDecimal getTransactionAmount() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0 ? debitAmount : creditAmount;
    }

    public String getTransactionDirection() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0 ? "DEBIT" : "CREDIT";
    }
}