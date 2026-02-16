package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderApprovalResponse {
    private Long orderId;
    private Long distributorId;
    private BigDecimal orderAmount;
    private BigDecimal accountBalance;
    private String status;
    private String message;
}