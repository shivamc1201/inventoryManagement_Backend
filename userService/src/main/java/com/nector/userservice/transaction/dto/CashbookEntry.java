package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashbookEntry {

    private String ledgerName;
    private LedgerType ledgerType;
    private UnderGroup underGroup;
    private BigDecimal grossAmount;
    private BigDecimal lessAdjustment;
    private BigDecimal amount;
}
