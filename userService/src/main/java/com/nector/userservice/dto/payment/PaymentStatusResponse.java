package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentStatusResponse {
    private Long paymentId;
    private String status;
    private Long distributorId;
    private String distributorName;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
}
