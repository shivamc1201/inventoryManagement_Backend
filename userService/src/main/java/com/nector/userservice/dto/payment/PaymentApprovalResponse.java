package com.nector.userservice.dto.payment;

import lombok.Data;

@Data
public class PaymentApprovalResponse {
    private Long paymentId;
    private String message;
    private String status;
}
