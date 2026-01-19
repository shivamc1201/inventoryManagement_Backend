package com.nector.userservice.ledger.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OpeningBalanceEvent {
    private Long accountId;
    private BigDecimal amount;
    private String description;
    private String createdBy;
}