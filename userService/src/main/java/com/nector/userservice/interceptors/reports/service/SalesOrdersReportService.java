package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesOrderRowDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SalesOrdersReportService {
    Page<SalesOrderRowDto> getOrderGrid(ReportFilterRequest filter);
    Map<String, Object> getStatusCounts(Long distributorId);
    List<Map<String, Object>> getSalesmanPerformance(LocalDate startDate, LocalDate endDate);
}
