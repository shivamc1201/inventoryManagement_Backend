package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MisSummaryDto {
    private MisKpiCardDto totalSales;
    private MisKpiCardDto totalPurchase;
    private MisKpiCardDto currentStockValue;
    private MisKpiCardDto outstandingReceivable;
}
