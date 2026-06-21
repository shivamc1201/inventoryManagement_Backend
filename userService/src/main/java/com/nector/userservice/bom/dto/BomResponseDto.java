package com.nector.userservice.bom.dto;

import com.nector.userservice.bom.dto.AdditionalCostDto;
import com.nector.userservice.bom.dto.BomComponentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomResponseDto {

    private Long id;
    private Long finishedProductId;
    private String finishedProductName;
    private String bomName;
    private BigDecimal outputQuantity;
    private String outputUnit;
    private Integer materialCount;
    private BigDecimal costAllocationPercent;
    private List<BomComponentDto> components;
    private List<AdditionalCostDto> additionalCosts;
    private BigDecimal totalComponentCost;
    private BigDecimal totalAdditionalCost;
    private BigDecimal effectiveCost;
    private BigDecimal effectiveRatePerUnit;
    private Instant createdAt;
    private Instant updatedAt;
}
