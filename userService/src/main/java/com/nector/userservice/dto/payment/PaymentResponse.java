package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long paymentId;
    private Long distributorId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
    private String status;
    private LocalDateTime processedAt;
}