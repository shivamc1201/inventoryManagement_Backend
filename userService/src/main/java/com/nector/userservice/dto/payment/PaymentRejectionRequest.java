package com.nector.userservice.dto.payment;

import lombok.Data;

@Data
public class PaymentRejectionRequest {
    private String reason;
}
