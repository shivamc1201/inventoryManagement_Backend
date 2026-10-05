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
public class MaterialInwardRowDto {
    private LocalDateTime date;
    private String itemName;
    private String batchNo;
    private String supplierName;
    private BigDecimal qtyReceived;
    private String voucherNo;
    private String warehouseLocation;
    private Long rawMaterialId;
}
