package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.ProductionReportService;
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
@RequestMapping("/api/reports/production")
@RequiredArgsConstructor
@Tag(name = "Production Reports", description = "Production log and BOM consumption report APIs")
public class ProductionReportController {

    private final ProductionReportService productionReportService;

    @GetMapping("/log")
    @Operation(summary = "Paginated production run log")
    public ResponseEntity<Page<ProductionLogDto>> getLog(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(productionReportService.getProductionLog(filter));
    }

    @GetMapping("/summary")
    @Operation(summary = "Production summary grouped by product")
    public ResponseEntity<List<Map<String, Object>>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(productionReportService.getProductionSummary(filter));
    }

    @GetMapping("/bom-consumption")
    @Operation(summary = "Planned vs actual raw material consumption per production run")
    public ResponseEntity<List<Map<String, Object>>> getBomConsumption(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(productionReportService.getBomConsumption(filter));
    }
}
