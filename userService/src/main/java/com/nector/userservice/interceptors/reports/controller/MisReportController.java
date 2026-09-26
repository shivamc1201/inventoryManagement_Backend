package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.MisDashboardResponse;
import com.nector.userservice.interceptors.reports.dto.MisKpiSummaryDto;
import com.nector.userservice.interceptors.reports.dto.MisRegionPerformanceDto;
import com.nector.userservice.interceptors.reports.dto.MisSalesTargetDto;
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

    /**
     * Consolidated dashboard endpoint — returns all sections in one call.
     * Supports filters: fromDate, toDate, financialYear, region, distributorId.
     * Region and distributorId are cascading: selecting a region scopes the distributor list;
     * selecting a distributor within that region applies both filters.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Full MIS dashboard: KPI cards with change%, weekly sales, top products, stock by category, " +
            "receivable aging, product performance table, region-wise tab, distributor outstanding tab")
    public ResponseEntity<MisDashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String financialYear,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Long distributorId) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(fromDate);
        filter.setEndDate(toDate);
        filter.setFinancialYear(financialYear);
        filter.setRegion(region);
        filter.setDistributorId(distributorId);
        return ResponseEntity.ok(misReportService.buildDashboard(filter));
    }

    @GetMapping("/kpi-summary")
    @Operation(summary = "Standalone KPI summary: total sales, purchase, stock value, collections, open orders, dispatch")
    public ResponseEntity<MisKpiSummaryDto> getKpiSummary(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String financialYear) {
        return ResponseEntity.ok(misReportService.getKpiSummary(buildFilter(distributorId, startDate, endDate, financialYear)));
    }

    @GetMapping("/recent-invoices")
    @Operation(summary = "Recent invoices for MIS dashboard")
    public ResponseEntity<List<SalesInvoiceRowDto>> getRecentInvoices(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(misReportService.getRecentInvoices(buildFilter(distributorId, startDate, endDate, null)));
    }

    @GetMapping("/sales-target")
    @Operation(summary = "Weekly actual sales (target is null until a target table is configured)")
    public ResponseEntity<List<MisSalesTargetDto>> getSalesTarget(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(misReportService.getSalesTarget(buildFilter(distributorId, startDate, endDate, null)));
    }

    @GetMapping("/region-performance")
    @Operation(summary = "Sales performance grouped by salesperson region")
    public ResponseEntity<List<MisRegionPerformanceDto>> getRegionPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(misReportService.getRegionPerformance(buildFilter(null, startDate, endDate, null)));
    }

    private ReportFilterRequest buildFilter(Long distributorId, LocalDate startDate, LocalDate endDate, String financialYear) {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setDistributorId(distributorId);
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        f.setFinancialYear(financialYear);
        return f;
    }
}
