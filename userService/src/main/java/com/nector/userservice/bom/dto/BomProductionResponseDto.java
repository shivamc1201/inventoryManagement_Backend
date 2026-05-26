package com.nector.userservice.bom.dto;

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
public class BomProductionResponseDto {

    private Long bomId;
    private String finishedProductName;
    private Long finishedProductId;
    private BigDecimal quantityProduced;
    private String outputUnit;
    private Integer finishedProductNewStock;
    private String batchNumber;
    private List<RawMaterialConsumptionDto> rawMaterialsConsumed;
    private Instant productionTime;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RawMaterialConsumptionDto {
        private Long rawMaterialId;
        private String rawMaterialName;
        private BigDecimal quantityRequired;
        private BigDecimal quantityAvailable;
        private String unit;
        private BigDecimal newStock;
    }
}
