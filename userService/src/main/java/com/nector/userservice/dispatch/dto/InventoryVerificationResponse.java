package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryVerificationResponse {
    private Long orderId;
    private String orderStatus;
    private List<InventoryItem> items;
    private boolean canProceedWithGdn;
    private String message;
    
    @Data
    public static class InventoryItem {
        private Long itemId;
        private String itemName;
        private String itemSku;
        private Integer orderedQuantity;
        private Integer availableQuantity;
        private boolean sufficientStock;
        private String status;
    }
}