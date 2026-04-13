package com.nector.userservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class VolumeAnalyticsResponse {
    private VolumeMetrics yearToDate;
    private VolumeMetrics monthToDate;
    private VolumeMetrics weekToDate;
    private Map<String, Long> volumeByRegion;
    private Map<String, Long> volumeByCategory;
    
    @Data
    public static class VolumeMetrics {
        private Long totalTransactions;
        private Long totalQuantity;
        private Double averageQuantityPerTransaction;
        
        public VolumeMetrics(Long totalTransactions, Long totalQuantity) {
            this.totalTransactions = totalTransactions;
            this.totalQuantity = totalQuantity;
            this.averageQuantityPerTransaction = totalTransactions > 0 ? 
                (double) totalQuantity / totalTransactions : 0.0;
        }
    }
}
