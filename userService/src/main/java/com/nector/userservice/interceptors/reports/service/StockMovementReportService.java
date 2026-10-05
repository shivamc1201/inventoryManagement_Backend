package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.MaterialInwardRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;
import com.nector.userservice.interceptors.reports.dto.SupplierInwardSummaryDto;

import java.util.List;

public interface StockMovementReportService {
    List<StockMovementRowDto> getLedger(ReportFilterRequest filter);
    List<MaterialInwardRowDto> getMaterialInward(ReportFilterRequest filter);
    List<SupplierInwardSummaryDto> getSupplierWiseSummary(ReportFilterRequest filter);
}
