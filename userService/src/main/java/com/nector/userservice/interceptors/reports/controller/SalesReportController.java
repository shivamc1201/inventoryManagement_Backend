package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ProductSalesRowDto;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(salesReportService.getInvoiceGrid(buildFilter(distributorId, dateFrom, dateTo, page, size)));
    }

    @GetMapping("/by-distributor")
    @Operation(summary = "Sales totals grouped by distributor")
    public ResponseEntity<List<Map<String, Object>>> getByDistributor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(salesReportService.getByDistributor(buildFilter(null, dateFrom, dateTo, 0, 50)));
    }

    @GetMapping("/by-dealer")
    @Operation(summary = "Sales totals grouped by dealer (from dealer invoices), includes distributor name")
    public ResponseEntity<List<Map<String, Object>>> getByDealer(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(salesReportService.getByDealer(buildFilter(distributorId, dateFrom, dateTo, 0, 50)));
    }

    @GetMapping("/monthly-trend")
    @Operation(summary = "Monthly sales revenue chart data")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(salesReportService.getMonthlyTrend(buildFilter(distributorId, dateFrom, dateTo, 0, 50)));
    }

    @GetMapping("/by-product")
    @Operation(summary = "Sales totals grouped by product (uses invoice line items)")
    public ResponseEntity<List<ProductSalesRowDto>> getByProduct(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(salesReportService.getByProduct(buildFilter(null, dateFrom, dateTo, 0, 50)));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Top N products by quantity sold (default top 10)")
    public ResponseEntity<List<ProductSalesRowDto>> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(salesReportService.getTopProducts(buildFilter(null, dateFrom, dateTo, 0, 50), limit));
    }

    private ReportFilterRequest buildFilter(Long distributorId, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setDistributorId(distributorId);
        f.setStartDate(dateFrom);
        f.setEndDate(dateTo);
        f.setPage(page);
        f.setSize(size);
        return f;
    }
}
