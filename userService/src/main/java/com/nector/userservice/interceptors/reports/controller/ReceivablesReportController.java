package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ReceivablesAgeingDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.ReceivablesReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/receivables")
@RequiredArgsConstructor
@Tag(name = "Receivables Reports", description = "Outstanding AR, ageing analysis and collection history APIs")
public class ReceivablesReportController {

    private final ReceivablesReportService receivablesReportService;

    @GetMapping("/outstanding")
    @Operation(summary = "Outstanding balance per dealer under a distributor")
    public ResponseEntity<List<ReceivablesAgeingDto>> getOutstanding(
            @RequestParam Long distributorId) {
        return ResponseEntity.ok(receivablesReportService.getOutstanding(distributorId));
    }

    @GetMapping("/ageing")
    @Operation(summary = "Ageing buckets: 0-30, 31-60, 61-90, 90+ days")
    public ResponseEntity<Map<String, Object>> getAgeingBuckets(
            @RequestParam Long distributorId) {
        return ResponseEntity.ok(receivablesReportService.getAgeingBuckets(distributorId));
    }

    @GetMapping("/collection-history")
    @Operation(summary = "Approved collection payments in date range")
    public ResponseEntity<List<Map<String, Object>>> getCollectionHistory(
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setDistributorId(distributorId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(receivablesReportService.getCollectionHistory(filter));
    }
}
