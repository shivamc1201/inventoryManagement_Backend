package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditBalanceResponse {
    private Long distributorId;
    private String distributorName;
    private BigDecimal creditLimit;
    private BigDecimal creditBalance;
    private BigDecimal creditUsed;
    private BigDecimal availablePercentage;
    private String status;
}

