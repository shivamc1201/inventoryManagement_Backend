package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDashboardResponse {
    private Long totalOrders;
    private BigDecimal totalAmount;
    private String period;
    
    public OrderDashboardResponse(Long totalOrders, BigDecimal totalAmount) {
        this.totalOrders = totalOrders;
        this.totalAmount = totalAmount;
    }
}
