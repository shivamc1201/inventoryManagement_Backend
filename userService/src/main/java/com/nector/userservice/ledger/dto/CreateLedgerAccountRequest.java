package com.nector.userservice.ledger.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new ledger account
 */
@Data
public class CreateLedgerAccountRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Distributor ID is required")
    private Long distributorId;

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name cannot exceed 255 characters")
    private String accountName;

    @DecimalMin(value = "0.00", message = "Credit limit cannot be negative")
    @Digits(integer = 15, fraction = 4, message = "Credit limit must have at most 15 integer digits and 4 decimal places")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    // Optional opening balance
    @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
    @Digits(integer = 15, fraction = 4, message = "Opening balance must have at most 15 integer digits and 4 decimal places")
    private BigDecimal openingBalance;

    @Size(max = 500, message = "Opening balance description cannot exceed 500 characters")
    private String openingBalanceDescription;
}