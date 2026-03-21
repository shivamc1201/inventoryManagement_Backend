package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class JournalVoucherEntry {
    private String accountNumber;
    private String accountName;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
}