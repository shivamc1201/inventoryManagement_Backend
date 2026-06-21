package com.nector.userservice.ledger.dto;

import com.nector.userservice.ledger.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new ledger transaction
 */
@Data
public class CreateTransactionRequest {

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNumber; // Optional - will be auto-generated if not provided

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal amount;

    private Long categoryId;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 100, message = "External reference cannot exceed 100 characters")
    private String externalReference;

    // For ADJUSTMENT transactions, specify if it's debit or credit
    private Boolean isDebit; // Only used for ADJUSTMENT type
}