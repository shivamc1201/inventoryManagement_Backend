package com.nector.userservice.transaction.repository;

import java.math.BigDecimal;

public interface LedgerCashbookAggregate {
    String getLedgerName();
    BigDecimal getGrossAmount();
    BigDecimal getLessAdjustment();
}
