package com.nector.userservice.bom.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class RawMaterialPriceHistoryDto {

    private Long id;
    private Long rawMaterialId;
    private String rawMaterialName;
    private String materialCode;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal priceChangePercent;
    private Instant changedAt;
}
