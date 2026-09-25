package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesOrderRowDto;
import com.nector.userservice.interceptors.reports.service.SalesOrdersReportService;
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
@RequestMapping("/api/reports/sales-orders")
@RequiredArgsConstructor
@Tag(name = "Sales Orders Reports", description = "Order pipeline and status count APIs")
public class SalesOrdersReportController {

    private final SalesOrdersReportService salesOrdersReportService;

    @GetMapping("/grid")
    @Operation(summary = "Paginated order tracking grid with status")
    public ResponseEntity<Page<SalesOrderRowDto>> getOrderGrid(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setDistributorId(distributorId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(salesOrdersReportService.getOrderGrid(filter));
    }

    @GetMapping("/status-count")
    @Operation(summary = "Count of orders by status (total, completed, pending)")
    public ResponseEntity<Map<String, Object>> getStatusCounts(
            @RequestParam(required = false) Long distributorId) {
        return ResponseEntity.ok(salesOrdersReportService.getStatusCounts(distributorId));
    }

    @PostMapping("/backfill-cart-weights")
    @Operation(summary = "Backfill total_weight on carts that are missing it (one-time fix)")
    public ResponseEntity<Map<String, Object>> backfillCartWeights() {
        return ResponseEntity.ok(salesOrdersReportService.backfillCartWeights());
    }

    @GetMapping("/salesman-performance")
    @Operation(summary = "Order count and total value grouped by salesperson")
    public ResponseEntity<List<Map<String, Object>>> getSalesmanPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(salesOrdersReportService.getSalesmanPerformance(startDate, endDate));
    }
}
