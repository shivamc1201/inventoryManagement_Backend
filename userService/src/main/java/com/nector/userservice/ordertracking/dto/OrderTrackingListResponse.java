package com.nector.userservice.ordertracking.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingListResponse {
    private List<OrderTrackingDTO> orders;
    private OrderTrackingStatsDTO stats;
    private long totalElements;
    private int totalPages;
}
