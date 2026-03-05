package com.nector.userservice.unit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponseDTO {
    private Long id;
    private String category;
    private String name;
    private String materialCode;
    private String sku;
    private String unit;
    private String unitType;
    private String productSize;
    private String unitName;
    private String unitCode;
    private String description;
    private String unitDescription;
    private String status;
    private String unitStatus;
    private BigDecimal price;
    private Integer quantity;
    private Integer minimumThreshold;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
