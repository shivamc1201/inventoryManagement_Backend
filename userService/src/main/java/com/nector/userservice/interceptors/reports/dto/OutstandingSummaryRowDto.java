package com.nector.userservice.interceptors.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OutstandingSummaryRowDto {
    private Long distributorId;
    private String distributorName;
    private long totalInvoices;
    private BigDecimal totalSales;
    private BigDecimal collectedAmount;
    private BigDecimal outstandingAmount;
    private long daysOverdue;
    private String status;
}
