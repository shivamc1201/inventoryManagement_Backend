package com.nector.userservice.interceptors.distributor.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderConfirmationResponse {
    private Long id;
    private Long orderId;
    private Long distributorId;
    private String gdnNumber;
    private OrderConfirmationRequest.ConfirmationStatus status;
    private Integer overallRating;
    private String feedback;
    private String remarks;
    private LocalDateTime confirmedAt;
    private OrderConfirmation.ApprovalStatus approvalStatus;
    private String adminComment;
    private List<ItemConfirmationResponse> itemConfirmations;
    
    @Data
    public static class ItemConfirmationResponse {
        private Long itemId;
        private String itemName;
        private Integer dispatchedQuantity;
        private Integer receivedQuantity;
        private OrderConfirmationRequest.ItemCondition condition;
        private String itemRemarks;
    }
}