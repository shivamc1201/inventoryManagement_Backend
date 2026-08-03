package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ReceivablesAgeingDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;

import java.util.List;
import java.util.Map;

public interface ReceivablesReportService {
    List<ReceivablesAgeingDto> getOutstanding(Long distributorId);
    Map<String, Object> getAgeingBuckets(Long distributorId);
    List<Map<String, Object>> getCollectionHistory(ReportFilterRequest filter);
}
