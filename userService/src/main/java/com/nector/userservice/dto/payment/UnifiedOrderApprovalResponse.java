package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UnifiedOrderApprovalResponse {

    private Long orderId;
    private Long distributorId;
    private BigDecimal orderAmount;

    // APPROVED | PENDING (insufficient) | ALREADY_APPROVED
    private String status;
    private String message;

    // Populated when status == APPROVED
    private BigDecimal ledgerBalanceUsed;
    private BigDecimal creditBalanceUsed;
    private BigDecimal remainingLedgerBalance;
    private BigDecimal remainingCreditBalance;

    // Populated when status == PENDING (insufficient)
    private BigDecimal availableLedgerBalance;
    private BigDecimal availableCreditBalance;
    private BigDecimal combinedAvailableBalance;
    private BigDecimal shortfall;
}
