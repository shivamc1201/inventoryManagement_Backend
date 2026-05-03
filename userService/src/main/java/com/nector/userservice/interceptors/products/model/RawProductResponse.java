package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RawProductResponse {
    private Long id;
    private String name;
    private String materialCode;
    private Unit unit;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal perItemPrice;
    private BigDecimal minimumThreshold;
    private String hsn;
    private BigDecimal taxRate;
    private Boolean active;
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String vendorId;
    private String vendorName;
    private String transportName;
    private String driverName;
    private String driverMobile;
    private ProductStatus status;
}