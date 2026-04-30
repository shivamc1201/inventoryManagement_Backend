package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScrapItemResponse {
    private Long id;
    private String name;
    private String itemCode;
    private Unit unit;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal perItemPrice;
    private Integer minimumThreshold;
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
