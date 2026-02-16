package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
class PendingPIResponse {
    private Long piId;
    private Long cartId;
    private Long distributorId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}

@Data
class BalanceUpdateRequest {
    private Long distributorId;
    private BigDecimal amount;
    private String transactionType; // CREDIT or DEBIT
    private String description;
}

@Data
class PaymentHistoryResponse {
    private Long id;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private LocalDateTime createdAt;
}