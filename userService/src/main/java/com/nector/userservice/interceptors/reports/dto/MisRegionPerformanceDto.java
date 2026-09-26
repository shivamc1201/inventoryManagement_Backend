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
public class MisRegionPerformanceDto {
    private String region;
    private BigDecimal totalSales;
    private long invoiceCount;
    private long distributorCount;
}
