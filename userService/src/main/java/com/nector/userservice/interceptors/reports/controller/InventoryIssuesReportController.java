package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.InventoryIssueRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.InventoryIssuesReportService;
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
@RequestMapping("/api/reports/inventory-issues")
@RequiredArgsConstructor
@Tag(name = "Inventory Issues Reports", description = "Spare parts and promotional item issue APIs")
public class InventoryIssuesReportController {

    private final InventoryIssuesReportService inventoryIssuesReportService;

    @GetMapping("/by-type")
    @Operation(summary = "Issues filtered by type: SPARE_PARTS, PROMOTIONAL_ITEMS, SCRAP_MATERIAL")
    public ResponseEntity<Page<InventoryIssueRowDto>> getByType(
            @RequestParam(defaultValue = "SPARE_PARTS") String itemType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(inventoryIssuesReportService.getByType(itemType, filter));
    }

    @GetMapping("/summary")
    @Operation(summary = "Issue totals grouped by item type in date range")
    public ResponseEntity<List<Map<String, Object>>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(inventoryIssuesReportService.getSummary(filter));
    }
}
