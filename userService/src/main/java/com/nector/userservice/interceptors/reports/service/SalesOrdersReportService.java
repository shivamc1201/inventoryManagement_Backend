package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesOrderRowDto;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface SalesOrdersReportService {
    Page<SalesOrderRowDto> getOrderGrid(ReportFilterRequest filter);
    Map<String, Object> getStatusCounts(Long distributorId);
}
