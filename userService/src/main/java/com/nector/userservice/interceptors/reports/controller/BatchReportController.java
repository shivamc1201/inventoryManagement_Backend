package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.BatchLifecycleDto;
import com.nector.userservice.interceptors.reports.service.BatchReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Management Reports", description = "Batch lifecycle and expiry alert APIs")
public class BatchReportController {

    private final BatchReportService batchReportService;

    @GetMapping("/lifecycle")
    @Operation(summary = "All active batches with quantity and expiry info")
    public ResponseEntity<List<BatchLifecycleDto>> getBatchLifecycle() {
        return ResponseEntity.ok(batchReportService.getBatchLifecycle());
    }

    @GetMapping("/expiry-alert")
    @Operation(summary = "Batches expiring within N days (default 30)")
    public ResponseEntity<List<BatchLifecycleDto>> getExpiryAlerts(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(batchReportService.getExpiryAlerts(daysAhead));
    }
}
