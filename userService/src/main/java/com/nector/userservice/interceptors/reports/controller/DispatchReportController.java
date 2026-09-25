package com.nector.userservice.interceptors.reports.controller;

import com.nector.userservice.interceptors.reports.dto.DeliveryConfirmationRowDto;
import com.nector.userservice.interceptors.reports.dto.DispatchRegisterRowDto;
import com.nector.userservice.interceptors.reports.dto.PendingDispatchRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.DispatchReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports/dispatch")
@RequiredArgsConstructor
@Tag(name = "Dispatch Reports", description = "GDN register and dispatch summary APIs")
public class DispatchReportController {

    private final DispatchReportService dispatchReportService;

    @GetMapping("/register")
    @Operation(summary = "Paginated GDN dispatch register. status: PENDING, IN_TRANSIT, DELIVERED")
    public ResponseEntity<Page<DispatchRegisterRowDto>> getRegister(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setDeliveryStatus(status);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(dispatchReportService.getRegister(filter));
    }

    @GetMapping("/summary")
    @Operation(summary = "Dispatch summary: total dispatches, weight, packages in period")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return ResponseEntity.ok(dispatchReportService.getSummary(filter));
    }

    @GetMapping("/pending")
    @Operation(summary = "Pending dispatch: GDNs not yet dispatched, with customer, items, amount and ready-since days")
    public ResponseEntity<Page<PendingDispatchRowDto>> getPendingDispatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(dispatchReportService.getPendingDispatches(filter));
    }

    @GetMapping("/delivery-confirmation")
    @Operation(summary = "Delivery confirmation report: delivered orders with POD status and received-by")
    public ResponseEntity<Page<DeliveryConfirmationRowDto>> getDeliveryConfirmations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ReportFilterRequest filter = new ReportFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPage(page);
        filter.setSize(size);
        return ResponseEntity.ok(dispatchReportService.getDeliveryConfirmations(filter));
    }
}
