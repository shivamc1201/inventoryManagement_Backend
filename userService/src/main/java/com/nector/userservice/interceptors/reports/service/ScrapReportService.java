package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.ScrapLifecycleRowDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ScrapReportService {
    List<ScrapLifecycleRowDto> getLifecycle(ReportFilterRequest filter);
    List<Map<String, Object>> getDisposalStatus(ReportFilterRequest filter);
    BigDecimal getTotalRevenue(ReportFilterRequest filter);
}
