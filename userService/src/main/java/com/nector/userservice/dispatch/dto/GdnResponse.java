package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GdnResponse {
    private Long id;
    private String gdnNumber;
    private Long orderId;
    private LocalDateTime gdnDate;
    private String dispatchFromAddress;
    private String shippingAddress;
    private String deliveryMethod;
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
    private BigDecimal totalWeight;
    private Integer totalPackages;
    private List<GdnItemResponse> gdnItems;
}