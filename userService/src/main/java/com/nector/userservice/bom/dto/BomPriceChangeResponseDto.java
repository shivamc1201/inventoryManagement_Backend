package com.nector.userservice.bom.dto;

import com.nector.userservice.bom.entity.BomPriceChangeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class BomPriceChangeResponseDto {

    private Long id;
    private Long rawMaterialId;
    private String rawMaterialName;
    private Long bomId;
    private String bomName;
    private Long bomComponentId;
    private Long finishedProductId;
    private String finishedProductName;
    private BigDecimal oldRate;
    private BigDecimal newRate;
    private BigDecimal priceChangePercent;
    private BomPriceChangeStatus status;
    private Long priceHistoryId;
    private Instant requestedAt;
    private Instant resolvedAt;
    private String resolvedBy;
    private String notes;
}
