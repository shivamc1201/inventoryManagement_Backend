package com.nector.userservice.bom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomSummaryDto {

    private Long totalBoms;
    private Long totalMaterials;
    private BigDecimal averageRatePerUnit;
}
