package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;
import com.nector.userservice.interceptors.reports.service.StockMovementReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/stock-movement")
@RequiredArgsConstructor
@Tag(name = "Stock Movement Reports", description = "Inward/outward stock ledger APIs")
public class StockMovementReportController {

    private final StockMovementReportService stockMovementReportService;

    @GetMapping("/ledger")
    @Operation(summary = "Combined inward + outward ledger for date range")
    public ResponseEntity<List<StockMovementRowDto>> getLedger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = buildFilter(null, startDate, endDate);
        return ResponseEntity.ok(stockMovementReportService.getLedger(filter));
    }

    @GetMapping("/inward")
    @Operation(summary = "Inward stock transactions (raw material lots received)")
    public ResponseEntity<List<StockMovementRowDto>> getInward(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockMovementReportService.getInwardSummary(buildFilter(null, startDate, endDate)));
    }

    @GetMapping("/outward")
    @Operation(summary = "Outward stock transactions (issues, scrap, promotions)")
    public ResponseEntity<List<StockMovementRowDto>> getOutward(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockMovementReportService.getOutwardSummary(buildFilter(null, startDate, endDate)));
    }

    private ReportFilterRequest buildFilter(Long distributorId, LocalDate startDate, LocalDate endDate) {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setDistributorId(distributorId);
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        return f;
    }
}
