package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;
import com.nector.userservice.interceptors.reports.service.ProductionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production-log")
@RequiredArgsConstructor
@Tag(name = "Production Log", description = "CRUD for recording actual production runs")
public class ProductionLogController {

    private final ProductionLogService productionLogService;

    @PostMapping
    @Operation(summary = "Record a new production run")
    public ResponseEntity<ProductionLogDto> create(@RequestBody ProductionLogDto dto) {
        return ResponseEntity.ok(productionLogService.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get production run by ID")
    public ResponseEntity<ProductionLogDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productionLogService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a production run")
    public ResponseEntity<ProductionLogDto> update(@PathVariable Long id, @RequestBody ProductionLogDto dto) {
        return ResponseEntity.ok(productionLogService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a production run record")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productionLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
