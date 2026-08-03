package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface SalesReportService {
    Page<SalesInvoiceRowDto> getInvoiceGrid(ReportFilterRequest filter);
    List<Map<String, Object>> getByDistributor(ReportFilterRequest filter);
    List<Map<String, Object>> getMonthlyTrend(ReportFilterRequest filter);
}
