package com.nector.userservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Data
public class VolumeAnalyticsResponse {
    private VolumeMetrics yearToDate;
    private VolumeMetrics monthToDate;
    private Map<String, Long> volumeByRegion;
    private Map<String, Long> volumeByCategory;

    // Combined from distributor-orders / orders dashboard
    private Long totalOrders;
    private BigDecimal totalAmount;
    private String period;

    // Monthly (current month MTD) and yearly (current year YTD) totals filtered by distributorId/salespersonId
    private BigDecimal totalAmountMonthly;
    private BigDecimal totalAmountYearly;

    // Outstanding amount = creditLimit - creditBalance (only when distributorId is provided)
    private BigDecimal totalOutstanding;

    @Data
    public static class VolumeMetrics {
        private Long totalTransactions;
        private BigDecimal totalVolumeTons;
        private BigDecimal averageVolumeTonsPerTransaction;

        public VolumeMetrics(Long totalTransactions, BigDecimal totalVolumeKg) {
            this.totalTransactions = totalTransactions;
            // Convert kg to tons (1 ton = 1000 kg) with 2 decimal places
            this.totalVolumeTons = totalVolumeKg != null
                ? totalVolumeKg.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
            this.averageVolumeTonsPerTransaction = totalTransactions > 0
                ? this.totalVolumeTons.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        }
    }
}
