package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogDto {
    private Long id;
    private String productionNumber;
    private Long bomId;
    private Long finishedProductId;
    private String finishedProductName;
    private String batchNumber;
    private BigDecimal quantityProduced;
    private String outputUnit;
    private LocalDate productionDate;
    private String shift;
    private String operatorName;
    private String supervisorName;
    private BigDecimal totalRawMaterialCost;
    private BigDecimal totalAdditionalCost;
    private BigDecimal totalProductionCost;
    private BigDecimal costPerUnit;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private List<ProductionLogComponentDto> components;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionLogComponentDto {
        private Long rawMaterialId;
        private String rawMaterialName;
        private BigDecimal quantityPlanned;
        private BigDecimal quantityActual;
        private String unit;
        private BigDecimal rate;
        private BigDecimal amount;
        private BigDecimal varianceQty;
    }
}
