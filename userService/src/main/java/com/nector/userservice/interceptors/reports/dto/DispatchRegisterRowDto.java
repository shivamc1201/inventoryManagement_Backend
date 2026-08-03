package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchRegisterRowDto {
    private Long id;
    private String gdnNumber;
    private Long orderId;
    private LocalDateTime gdnDate;
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
    private Integer totalPackages;
    private BigDecimal totalWeight;
    private String shippingAddress;
    private int itemCount;
}
