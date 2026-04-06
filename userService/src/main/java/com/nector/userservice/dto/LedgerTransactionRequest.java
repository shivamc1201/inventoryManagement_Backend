package com.nector.userservice.dto;

import com.nector.userservice.enums.LedgerTransactionCategory;
import com.nector.userservice.enums.LedgerTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerTransactionRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
    
    @NotNull(message = "Distributor ID is required")
    private Long distributorId;

    private LocalDate date;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Reference cannot exceed 100 characters")
    private String reference;

    private LedgerTransactionType type;

    private BigDecimal amount;

    private LedgerTransactionCategory category;
}
