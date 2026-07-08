package com.nector.userservice.controller;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.service.EmployeeKpiAssignmentService;
import com.nector.userservice.service.EmployeeKpiResultService;
import com.nector.userservice.service.KpiExportService;
import com.nector.userservice.service.KpiMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/kra-kpi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KRA/KPI Admin", description = "Admin APIs for managing KRA/KPI master data and assignments")
public class KpiAdminController {

    private final KpiMasterService kpiMasterService;
    private final EmployeeKpiAssignmentService assignmentService;
    private final EmployeeKpiResultService resultService;
    private final KpiExportService exportService;

    // ============ KPI Master Management ============

    @PostMapping("/create")
    @Operation(summary = "Create KPI/KRA Master", description = "Create a new KPI or KRA definition")
    public ResponseEntity<KpiMasterResponse> createKpi(
            @Valid @RequestBody KpiMasterCreateRequest request) {
        
        Long createdBy = 1L; // Default user ID
        KpiMasterResponse response = kpiMasterService.createKpi(request, createdBy);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update KPI/KRA", description = "Partially update an existing KPI or KRA definition — only provided fields are changed")
    public ResponseEntity<KpiMasterResponse> updateKpi(
            @Parameter(description = "KPI ID") @PathVariable Long id,
            @Valid @RequestBody KpiMasterUpdateRequest request) {
        
        KpiMasterResponse response = kpiMasterService.updateKpi(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete KPI/KRA", description = "Soft delete a KPI or KRA definition")
    public ResponseEntity<Void> deleteKpi(
            @Parameter(description = "KPI ID") @PathVariable Long id) {
        
        kpiMasterService.deleteKpi(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/master/{id}")
    @Operation(summary = "Get KPI by ID", description = "Get KPI master details by ID")
    public ResponseEntity<KpiMasterResponse> getKpiById(
            @Parameter(description = "KPI ID") @PathVariable Long id) {
        
        KpiMasterResponse response = kpiMasterService.getKpiById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master")
    @Operation(summary = "Get All KPIs", description = "Get all active KPI masters")
    public ResponseEntity<List<KpiMasterResponse>> getAllKpis() {
        List<KpiMasterResponse> response = kpiMasterService.getAllActiveKpis();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master/category/{category}")
    @Operation(summary = "Get KPIs by Category", description = "Get KPIs filtered by category (KRA or KPI)")
    public ResponseEntity<List<KpiMasterResponse>> getKpisByCategory(
            @Parameter(description = "KPI Category") @PathVariable KPICategory category) {
        
        List<KpiMasterResponse> response = kpiMasterService.getKpisByCategory(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master/frequency/{frequency}")
    @Operation(summary = "Get KPIs by Frequency", description = "Get KPIs filtered by frequency")
    public ResponseEntity<List<KpiMasterResponse>> getKpisByFrequency(
            @Parameter(description = "KPI Frequency") @PathVariable KPIFrequency frequency) {
        
        List<KpiMasterResponse> response = kpiMasterService.getKpisByFrequency(frequency);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master/search")
    @Operation(summary = "Search KPIs", description = "Search KPIs by code, name, or description")
    public ResponseEntity<List<KpiMasterResponse>> searchKpis(
            @Parameter(description = "Search query") @RequestParam String query) {
        
        List<KpiMasterResponse> response = kpiMasterService.searchKpis(query);
        return ResponseEntity.ok(response);
    }

    // ============ KPI Assignment ============

    @PostMapping("/assign")
    @Operation(summary = "Assign KPI to Employee", description = "Assign a KPI to an employee with target and weightage")
    public ResponseEntity<KpiAssignmentResponse> assignKpi(
            @Valid @RequestBody KpiAssignmentRequest request) {
        
        Long assignedBy = 1L; // Default user ID
        KpiAssignmentResponse response = assignmentService.assignKpiToEmployee(request, assignedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign/bulk")
    @Operation(summary = "Bulk Assign KPIs to Employee", description = "Assign multiple KPIs to an employee at once with weightage validation")
    public ResponseEntity<KpiBulkAssignmentResponse> bulkAssignKpis(
            @Valid @RequestBody KpiBulkAssignmentRequest request) {
        
        Long assignedBy = 1L; // Default user ID
        KpiBulkAssignmentResponse response = assignmentService.bulkAssignKpisToEmployee(request, assignedBy);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/assign/{id}")
    @Operation(summary = "Update KPI Assignment", description = "Update an existing KPI assignment")
    public ResponseEntity<KpiAssignmentResponse> updateAssignment(
            @Parameter(description = "Assignment ID") @PathVariable Long id,
            @Valid @RequestBody KpiAssignmentRequest request) {
        
        KpiAssignmentResponse response = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assign/{id}")
    @Operation(summary = "Delete KPI Assignment", description = "Delete a KPI assignment")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(description = "Assignment ID") @PathVariable Long id) {
        
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ============ Generate Results ============

    @PostMapping("/generate-result/{employeeId}")
    @Operation(summary = "Generate KPI Result", description = "Generate KPI result for an employee for a specific month/year")
    public ResponseEntity<KpiResultResponse> generateResult(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Month (1-12)") @RequestParam Integer month,
            @Parameter(description = "Year") @RequestParam Integer year) {
        
        KpiResultResponse response = resultService.generateResult(employeeId, month, year);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-results")
    @Operation(summary = "Generate Results for All", description = "Generate KPI results for all employees for a specific month/year")
    public ResponseEntity<List<KpiResultResponse>> generateResultsForAll(
            @Parameter(description = "Month (1-12)") @RequestParam Integer month,
            @Parameter(description = "Year") @RequestParam Integer year) {
        
        List<KpiResultResponse> response = resultService.generateResultsForAllEmployees(month, year);
        return ResponseEntity.ok(response);
    }

    // ============ Reports ============

    @GetMapping("/report/assignments")
    @Operation(summary = "Get Assignment Report", description = "Get paginated assignment report with filters")
    public ResponseEntity<Page<KpiAssignmentResponse>> getAssignmentReport(
            @Valid KpiReportFilterRequest filter,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<KpiAssignmentResponse> response = assignmentService.getAllAssignments(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/employee/{employeeId}")
    @Operation(summary = "Employee-wise Report", description = "Get KPI report for a specific employee")
    public ResponseEntity<List<KpiAssignmentResponse>> getEmployeeReport(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        List<KpiAssignmentResponse> response = assignmentService.getAssignmentsByEmployee(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/all-assignments")
    @Operation(summary = "Get All Assignments", description = "Get all KPI assignments for all employees")
    public ResponseEntity<List<KpiAssignmentResponse>> getAllAssignments() {

        List<KpiAssignmentResponse> response = assignmentService.getAllAssignments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/month/{month}/{year}")
    @Operation(summary = "Month-wise Report", description = "Get KPI results for a specific month and year")
    public ResponseEntity<List<KpiResultResponse>> getMonthWiseReport(
            @Parameter(description = "Month (1-12)") @PathVariable Integer month,
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        List<KpiResultResponse> response = resultService.getResultsByMonthAndYear(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/year/{year}")
    @Operation(summary = "Year-wise Report", description = "Get KPI results for a specific year")
    public ResponseEntity<List<KpiResultResponse>> getYearWiseReport(
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        List<KpiResultResponse> response = resultService.getResultsByYear(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/top-performers")
    @Operation(summary = "Top Performers", description = "Get top performers for a specific period")
    public ResponseEntity<List<KpiResultResponse>> getTopPerformers(
            @Parameter(description = "Month (optional)") @RequestParam(required = false) Integer month,
            @Parameter(description = "Year") @RequestParam Integer year,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        
        List<KpiResultResponse> response;
        if (month != null) {
            response = resultService.getTopPerformers(month, year, limit);
        } else {
            response = resultService.getTopPerformersByYear(year, limit);
        }
        return ResponseEntity.ok(response);
    }

    // ============ Export APIs ============

    @GetMapping("/export/assignments/excel")
    @Operation(summary = "Export Assignments to Excel", description = "Export KPI assignments to Excel file")
    public ResponseEntity<byte[]> exportAssignmentsToExcel(@Valid KpiReportFilterRequest filter) {
        byte[] excelData = exportService.exportAssignmentsToExcel(filter);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "kpi-assignments.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/export/results/excel")
    @Operation(summary = "Export Results to Excel", description = "Export KPI results to Excel file")
    public ResponseEntity<byte[]> exportResultsToExcel(@Valid KpiReportFilterRequest filter) {
        byte[] excelData = exportService.exportResultsToExcel(filter);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "kpi-results.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/export/assignments/pdf")
    @Operation(summary = "Export Assignments to PDF", description = "Export KPI assignments to PDF file")
    public ResponseEntity<byte[]> exportAssignmentsToPdf(@Valid KpiReportFilterRequest filter) {
        byte[] pdfData = exportService.exportAssignmentsToPdf(filter);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "kpi-assignments.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }

    @GetMapping("/export/results/pdf")
    @Operation(summary = "Export Results to PDF", description = "Export KPI results to PDF file")
    public ResponseEntity<byte[]> exportResultsToPdf(@Valid KpiReportFilterRequest filter) {
        byte[] pdfData = exportService.exportResultsToPdf(filter);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "kpi-results.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }

    // ============ Scheduler Manual Triggers ============

    @PostMapping("/rollover-assignments")
    @Operation(
        summary = "Rollover KPI assignments to current month",
        description = "Manually triggers the monthly rollover that copies previous month's KPI assignments " +
                      "to the current month with achievedValue reset to 0. Safe to call multiple times — " +
                      "skips assignments that already exist for the current month."
    )
    public ResponseEntity<Map<String, Object>> rolloverAssignments() {
        log.info("Manual rollover triggered via admin API");
        try {
            assignmentService.rolloverAssignmentsToNextMonth();
            return ResponseEntity.ok(Map.of(
                "message", "KPI assignments rolled over to current month successfully"
            ));
        } catch (Exception e) {
            log.error("Manual rollover failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate-monthly-results")
    @Operation(
        summary = "Generate monthly KPI results for all employees",
        description = "Manually triggers result generation for a given month and year. " +
                      "Defaults to the previous month if no params provided."
    )
    public ResponseEntity<Map<String, Object>> generateMonthlyResults(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        java.time.LocalDate ref = (month == null || year == null)
                ? java.time.LocalDate.now().minusMonths(1)
                : java.time.LocalDate.of(year, month, 1);
        int m = ref.getMonthValue();
        int y = ref.getYear();
        log.info("Manual monthly result generation triggered for {}/{}", m, y);
        try {
            resultService.generateResultsForAllEmployees(m, y);
            return ResponseEntity.ok(Map.of(
                "message", "Monthly results generated for " + m + "/" + y
            ));
        } catch (Exception e) {
            log.error("Manual result generation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
