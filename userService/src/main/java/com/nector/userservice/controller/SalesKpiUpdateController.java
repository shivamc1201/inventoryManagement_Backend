package com.nector.userservice.controller;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.service.SalesKpiUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales-kpi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales KPI Update", description = "3rd party API to push daily sales KPI data")
public class SalesKpiUpdateController {

    private final SalesKpiUpdateService salesKpiUpdateService;

    @PostMapping("/update")
    @Operation(summary = "Save Sales KPI Update", description = "Accepts daily salesperson KPI data from 3rd party and persists it")
    public ResponseEntity<CustomResponse> update(@Valid @RequestBody List<SalesKpiUpdateRequest> requests) {
        CustomResponse response = salesKpiUpdateService.saveAll(requests);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    @Operation(
        summary = "Recalculate KPI achievements for a month",
        description = "Resets all KPI achieved values for the given month and replays stored meeting details to recompute. " +
                      "Defaults to current month if month/year not supplied. Safe to call multiple times."
    )
    public ResponseEntity<Map<String, Object>> recalculate(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate ref = (month == null || year == null) ? LocalDate.now() : LocalDate.of(year, month, 1);
        int m = ref.getMonthValue();
        int y = ref.getYear();
        log.info("KPI recalculate triggered for {}/{}", m, y);
        Map<String, Object> result = salesKpiUpdateService.recalculateForMonth(m, y);
        return ResponseEntity.ok(result);
    }
}
