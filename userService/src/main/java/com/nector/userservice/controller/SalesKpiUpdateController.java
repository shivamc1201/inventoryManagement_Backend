package com.nector.userservice.controller;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.service.SalesKpiUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-kpi")
@RequiredArgsConstructor
@Tag(name = "Sales KPI Update", description = "3rd party API to push daily sales KPI data")
public class SalesKpiUpdateController {

    private final SalesKpiUpdateService salesKpiUpdateService;

    @PostMapping("/update")
    @Operation(summary = "Save Sales KPI Update", description = "Accepts daily salesperson KPI data from 3rd party and persists it")
    public ResponseEntity<CustomResponse> update(@Valid @RequestBody List<SalesKpiUpdateRequest> requests) {
        CustomResponse response = salesKpiUpdateService.saveAll(requests);
        return ResponseEntity.ok(response);
    }
}
