package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.MaterialInwardRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;
import com.nector.userservice.interceptors.reports.dto.SupplierInwardSummaryDto;
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
    @Operation(summary = "Stock ledger: combined inward + outward with running balance")
    public ResponseEntity<List<StockMovementRowDto>> getLedger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = buildFilter(startDate, endDate, null);
        return ResponseEntity.ok(stockMovementReportService.getLedger(filter));
    }

    @GetMapping("/material-inward")
    @Operation(summary = "Material inward report: item name, batch no, supplier, voucher no, warehouse")
    public ResponseEntity<List<MaterialInwardRowDto>> getMaterialInward(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String supplierId) {
        ReportFilterRequest filter = buildFilter(startDate, endDate, supplierId);
        return ResponseEntity.ok(stockMovementReportService.getMaterialInward(filter));
    }

    @GetMapping("/supplier-wise")
    @Operation(summary = "Supplier-wise inward summary: receipt count, total qty, last receipt date, top item")
    public ResponseEntity<List<SupplierInwardSummaryDto>> getSupplierWise(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockMovementReportService.getSupplierWiseSummary(buildFilter(startDate, endDate, null)));
    }

    private ReportFilterRequest buildFilter(LocalDate startDate, LocalDate endDate, String supplierId) {
        ReportFilterRequest f = new ReportFilterRequest();
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        f.setSupplierId(supplierId);
        return f;
    }
}
