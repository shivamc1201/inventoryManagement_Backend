package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;

import java.util.List;

public interface StockMovementReportService {
    List<StockMovementRowDto> getLedger(ReportFilterRequest filter);
    List<StockMovementRowDto> getInwardSummary(ReportFilterRequest filter);
    List<StockMovementRowDto> getOutwardSummary(ReportFilterRequest filter);
}
