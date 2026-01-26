package com.nector.userservice.interceptors.products.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinishedProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private Integer minimumThreshold;
    private Boolean active;
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}