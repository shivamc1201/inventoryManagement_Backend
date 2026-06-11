package com.nector.userservice.dispatch.dto;

import lombok.Data;

/**
 * Simple GDN generation request for dispatch team
 * Uses live cart data automatically - no need to specify items
 */
@Data
public class SimpleGdnRequest {
    private String dispatchFromAddress;
    private String shippingAddress;
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
    private String deliveryMethod;
}
