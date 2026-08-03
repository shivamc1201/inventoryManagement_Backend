package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import com.nector.userservice.interceptors.reports.service.SalesReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Reports", description = "Sales invoice grid, trend and distributor grouping APIs")
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping("/invoice-grid")
    @Operation(summary = "Paginated invoice list with filters")
    public ResponseEntity<Page<SalesInvoiceRowDto>> getInvoiceGrid(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(salesReportService.getInvoiceGrid(buildFilter(distributorId, startDate, endDate, page, size)));
    }

    @GetMapping("/by-distributor")
    @Operation(summary = "Sales totals grouped by distributor")
    public ResponseEntity<List<Map<String, Object>>> getByDistributor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(salesReportService.getByDistributor(buildFilter(null, startDate, endDate, 0, 50)));
    }

    @GetMapping("/monthly-trend")
    @Operation(summary = "Monthly sales revenue chart data")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(salesReportService.getMonthlyTrend(buildFilter(distributorId, startDate, endDate, 0, 50)));
    }

    private ReportFilterRequest buildFilter(Long distributorId, LocalDate startDate, LocalDate endDate, int page, int size) {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setDistributorId(distributorId);
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        f.setPage(page);
        f.setSize(size);
        return f;
    }
}
