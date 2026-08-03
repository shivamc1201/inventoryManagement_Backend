package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MisKpiSummaryDto {
    private BigDecimal totalSalesValue;
    private BigDecimal totalCollections;
    private long openOrdersCount;
    private long dispatchVolume;
    private List<Map<String, Object>> monthlyTrend;
    private List<Map<String, Object>> distributorRanking;
}
