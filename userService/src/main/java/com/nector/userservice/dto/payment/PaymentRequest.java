package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long distributorId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
}