package com.nector.userservice.interceptors.distributor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DistributorStockResponse {
    private Long distributorId;
    private String distributorName;
    private Integer totalUniqueItems;
    private Integer totalQuantity;
    private BigDecimal totalStockValue;
    private LocalDateTime lastUpdated;
    private List<StockItem> stockItems;

    @Data
    public static class StockItem {
        private Long itemId;
        private String itemName;
        private String itemSku;
        private Integer totalQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
        private String unitType;
    }
}
