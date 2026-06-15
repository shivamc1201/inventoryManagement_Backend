package com.nector.userservice.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashbookSummary {

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<CashbookEntry> entries;
}
