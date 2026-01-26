package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.RawProduct;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RawProductResponse {
    private Long id;
    private String name;
    private String materialCode;
    private RawProduct.Unit unit;
    private Integer quantity;
    private Integer minimumThreshold;
    private Boolean active;
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}