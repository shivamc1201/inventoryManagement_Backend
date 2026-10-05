package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInwardSummaryDto {
    private String supplierId;
    private String supplierName;
    private Long receiptsCount;
    private BigDecimal totalQtyReceived;
    private LocalDateTime lastReceiptDate;
    private String topItem;
}
