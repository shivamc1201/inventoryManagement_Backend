package com.nector.userservice.dto.payment;

import lombok.Data;

@Data
public class PaymentStatusResponse {
    private Long paymentId;
    private String status;
}
