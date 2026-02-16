package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class GdnGenerationRequest {
    private String dispatchFromAddress;
    private String shippingAddress;
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
    private List<InventoryVerificationItem> verifiedItems;
    
    @Data
    public static class InventoryVerificationItem {
        private Long itemId;
        private Integer orderedQuantity;
        private Integer availableQuantity;
        private Integer dispatchQuantity;
        private String remarks;
    }
}