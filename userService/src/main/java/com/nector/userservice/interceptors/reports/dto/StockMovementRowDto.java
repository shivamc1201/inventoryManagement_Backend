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
public class StockMovementRowDto {
    private String movementType;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private String reference;
    private LocalDateTime date;
}
