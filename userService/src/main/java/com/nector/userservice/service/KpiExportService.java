package com.nector.userservice.service;

import com.nector.userservice.dto.KpiReportFilterRequest;

public interface KpiExportService {

    byte[] exportAssignmentsToExcel(KpiReportFilterRequest filter);

    byte[] exportResultsToExcel(KpiReportFilterRequest filter);

    byte[] exportAssignmentsToPdf(KpiReportFilterRequest filter);

    byte[] exportResultsToPdf(KpiReportFilterRequest filter);
}
