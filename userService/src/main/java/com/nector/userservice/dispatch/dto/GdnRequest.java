package com.nector.userservice.dispatch.dto;

import lombok.Data;

@Data
public class GdnRequest {
    private String dispatchFromAddress;
    private String shippingAddress;
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
}