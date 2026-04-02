package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerTransactionResponse {

    private String id;
    private Long dealerId;
    private Long distributorId;
    private String date;
    private String description;
    private String reference;
    private String type;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
    private String category;
    private LocalDateTime createdAt;
}
