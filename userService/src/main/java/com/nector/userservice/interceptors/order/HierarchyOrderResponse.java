package com.nector.userservice.interceptors.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One order row for the sales "Hierarchy Orders" screen. */
@Data
@Builder
public class HierarchyOrderResponse {
    /** Cart id; this is the id PUT /api/order/approve/{cartId} expects. */
    private Long orderId;
    private Long distributorId;
    private String distributorName;
    private BigDecimal amount;
    /** pending | approved | completed | rejected */
    private String status;
    private LocalDateTime createdAt;
    private Long salespersonId;
    private String salespersonName;
    private String salespersonRole;
    private String zone;
}
