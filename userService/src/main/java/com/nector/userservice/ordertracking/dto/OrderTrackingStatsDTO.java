package com.nector.userservice.ordertracking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingStatsDTO {
    private long totalOrders;
    private long pendingOrders;     // has any pending/in-progress step
    private long completedOrders;   // all 11 steps completed
}
