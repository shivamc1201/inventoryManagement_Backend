package com.nector.userservice.interceptors.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OutstandingSummaryResponse {
    private BigDecimal totalOutstanding;
    private BigDecimal overdueAmount;
    private BigDecimal collectedTotal;
    private long avgDso;
    private List<OutstandingSummaryRowDto> rows;
}
