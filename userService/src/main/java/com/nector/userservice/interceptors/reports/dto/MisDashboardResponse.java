package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MisDashboardResponse {
    private MisSummaryDto summary;
    private List<MisSalesTargetDto> salesTarget;
    private List<ProductSalesRowDto> topProducts;
    private List<Map<String, Object>> stockByCategory;
    private Map<String, Object> receivableAging;
    private List<ProductSalesRowDto> productPerformance;
    private List<MisRegionPerformanceDto> regionPerformance;
    private List<DistributorOutstandingRowDto> distributorOutstanding;
}
