package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class GdnGenerationRequest {
    private static final String DEFAULT_DISPATCH_ADDRESS = "Nectar Origin Private Limited\nPlot No 152/ 952, Salempur Saini , Khalgaon Barahat Bypass Road , Bhagalpur , Bihar -813222";
    
    private String dispatchFromAddress = DEFAULT_DISPATCH_ADDRESS;
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