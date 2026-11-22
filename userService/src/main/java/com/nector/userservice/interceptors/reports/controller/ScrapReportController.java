package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.ScrapLifecycleRowDto;
import com.nector.userservice.interceptors.reports.service.ScrapReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/scrap")
@RequiredArgsConstructor
@Tag(name = "Scrap Management Reports", description = "Scrap lifecycle, disposal status and revenue APIs")
public class ScrapReportController {

    private final ScrapReportService scrapReportService;

    @GetMapping("/lifecycle")
    @Operation(summary = "Scrap outward approval lifecycle (generation → disposal → sale)")
    public ResponseEntity<List<ScrapLifecycleRowDto>> getLifecycle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(scrapReportService.getLifecycle(filter));
    }

    @GetMapping("/disposal-status")
    @Operation(summary = "Scrap disposal grouped by approval status with value")
    public ResponseEntity<List<Map<String, Object>>> getDisposalStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(scrapReportService.getDisposalStatus(filter));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Total approved scrap sale revenue in date range")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(scrapReportService.getTotalRevenue(filter));
    }
}
