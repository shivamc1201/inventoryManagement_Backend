package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductionReportService {
    Page<ProductionLogDto> getProductionLog(ReportFilterRequest filter);
    List<Map<String, Object>> getProductionSummary(ReportFilterRequest filter);
    List<Map<String, Object>> getBomConsumption(ReportFilterRequest filter);
}
