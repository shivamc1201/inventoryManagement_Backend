package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinishedProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private Unit unit;
    private BigDecimal weight;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal perPieceRate;
    private Integer minimumThreshold;
    private Boolean active;
    private Boolean lowStock;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProductStatus status;
}
