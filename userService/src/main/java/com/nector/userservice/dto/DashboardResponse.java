package com.nector.userservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class DashboardResponse {
    private SalesMetrics yearToDate;
    private SalesMetrics monthToDate;
    private SalesMetrics weekToDate;
    private Map<String, BigDecimal> salesByRegion;
    private Map<String, BigDecimal> salesByCategory;
    private Long totalOrders;
    private BigDecimal totalAmount;
    private UserStats userStats;

    @Data
    public static class SalesMetrics {
        private BigDecimal totalSales;
        private Long transactionCount;
        private BigDecimal averageOrderValue;

        public SalesMetrics(BigDecimal totalSales, Long transactionCount) {
            this.totalSales = totalSales;
            this.transactionCount = transactionCount;
            this.averageOrderValue = transactionCount > 0 ?
                totalSales.divide(BigDecimal.valueOf(transactionCount), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        }
    }

    @Data
    public static class UserStats {
        private Long users;
        private Long salespersons;
        private Long distributors;
        private Long dealers;
        private Long totalUsers;
    }
}