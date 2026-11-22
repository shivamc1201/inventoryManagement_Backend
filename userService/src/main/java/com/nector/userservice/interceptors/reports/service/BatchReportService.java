package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.BatchLifecycleDto;

import java.util.List;

public interface BatchReportService {
    List<BatchLifecycleDto> getBatchLifecycle();
    List<BatchLifecycleDto> getExpiryAlerts(int daysAhead);
}
