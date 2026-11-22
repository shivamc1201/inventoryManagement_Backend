package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.InventorySnapshotDto;
import com.nector.userservice.interceptors.reports.service.InventoryReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Reports", description = "Inventory snapshot and valuation report APIs")
public class InventoryReportController {

    private final InventoryReportService inventoryReportService;

    @GetMapping("/snapshot")
    @Operation(summary = "Current stock snapshot by category (RAW, FINISHED, SCRAP)")
    public ResponseEntity<List<InventorySnapshotDto>> getSnapshot(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(inventoryReportService.getSnapshot(category));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Items at or below minimum threshold")
    public ResponseEntity<List<InventorySnapshotDto>> getLowStock(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(inventoryReportService.getLowStock(category));
    }
}
