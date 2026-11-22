package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySnapshotDto {
    private Long id;
    private String category;
    private String itemName;
    private String sku;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private Integer minimumThreshold;
    private boolean belowThreshold;
}
