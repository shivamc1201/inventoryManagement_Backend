package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.MisKpiSummaryDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import com.nector.userservice.interceptors.reports.service.MisReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/mis")
@RequiredArgsConstructor
@Tag(name = "MIS Reports", description = "MIS Dashboard report APIs")
public class MisReportController {

    private final MisReportService misReportService;

    @GetMapping("/kpi-summary")
    @Operation(summary = "Get KPI summary for MIS dashboard")
    public ResponseEntity<MisKpiSummaryDto> getKpiSummary(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String financialYear) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setDistributorId(distributorId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setFinancialYear(financialYear);
        return ResponseEntity.ok(misReportService.getKpiSummary(filter));
    }

    @GetMapping("/recent-invoices")
    @Operation(summary = "Get recent invoices for MIS dashboard")
    public ResponseEntity<List<SalesInvoiceRowDto>> getRecentInvoices(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setDistributorId(distributorId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(misReportService.getRecentInvoices(filter));
    }
}
