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
public class DistributorOutstandingRowDto {
    private Long distributorId;
    private String distributorName;
    private long totalInvoices;
    private BigDecimal totalSales;
    private BigDecimal receivedAmount;
    private BigDecimal outstandingAmount;
}
