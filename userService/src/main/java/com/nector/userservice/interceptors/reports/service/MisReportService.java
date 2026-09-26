package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.MisDashboardResponse;
import com.nector.userservice.interceptors.reports.dto.MisKpiSummaryDto;
import com.nector.userservice.interceptors.reports.dto.MisRegionPerformanceDto;
import com.nector.userservice.interceptors.reports.dto.MisSalesTargetDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;

import java.util.List;

public interface MisReportService {
    MisKpiSummaryDto getKpiSummary(ReportFilterRequest filter);
    List<SalesInvoiceRowDto> getRecentInvoices(ReportFilterRequest filter);
    List<MisSalesTargetDto> getSalesTarget(ReportFilterRequest filter);
    List<MisRegionPerformanceDto> getRegionPerformance(ReportFilterRequest filter);
    MisDashboardResponse buildDashboard(ReportFilterRequest filter);
}
