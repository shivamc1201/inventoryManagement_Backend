package com.nector.userservice.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OrderSummaryResponse {

    private long totalOrders;
    private Map<String, StatusSummary> byStatus;

    @Data
    public static class StatusSummary {
        private long count;
    }
}
