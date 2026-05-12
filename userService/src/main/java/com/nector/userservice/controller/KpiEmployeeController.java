package com.nector.userservice.controller;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.service.EmployeeKpiAssignmentService;
import com.nector.userservice.service.EmployeeKpiResultService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/kra-kpi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KRA/KPI Employee", description = "Employee APIs for viewing and updating their own KPI performance")
public class KpiEmployeeController {

    private final EmployeeKpiAssignmentService assignmentService;
    private final EmployeeKpiResultService resultService;

    // ============ Dashboard ============

    @GetMapping("/dashboard/{employeeId}")
    @Operation(summary = "Employee KPI Dashboard", description = "Get complete KPI dashboard for an employee")
    public ResponseEntity<EmployeeKpiDashboardResponse> getDashboard(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        EmployeeKpiDashboardResponse response = assignmentService.getEmployeeDashboard(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/me/{employeeId}")
    @Operation(summary = "My KPI Dashboard", description = "Get KPI dashboard for the specified employee")
    public ResponseEntity<EmployeeKpiDashboardResponse> getMyDashboard(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        EmployeeKpiDashboardResponse response = assignmentService.getEmployeeDashboard(employeeId);
        return ResponseEntity.ok(response);
    }

    // ============ View Assignments ============

    @GetMapping("/assignments/{employeeId}")
    @Operation(summary = "My KPI Assignments", description = "Get all KPI assignments for an employee")
    public ResponseEntity<Page<KpiAssignmentResponse>> getMyAssignments(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<KpiAssignmentResponse> response = assignmentService.getAssignmentsByEmployee(employeeId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{employeeId}/status/{status}")
    @Operation(summary = "My KPI Assignments by Status", description = "Get KPI assignments filtered by status")
    public ResponseEntity<List<KpiAssignmentResponse>> getMyAssignmentsByStatus(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Status") @PathVariable KPIStatus status) {
        
        List<KpiAssignmentResponse> response = assignmentService.getAssignmentsByEmployeeAndStatus(employeeId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{employeeId}/active")
    @Operation(summary = "My Active KPIs", description = "Get current active KPI assignments")
    public ResponseEntity<List<KpiAssignmentResponse>> getMyActiveAssignments(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        // Temporarily removed authentication for testing
        // validateEmployeeAccess(employeeId, userDetails);
        
        List<KpiAssignmentResponse> response = assignmentService.getCurrentActiveAssignments(employeeId);
        return ResponseEntity.ok(response);
    }

    // ============ Update Progress ============

    @PutMapping("/update-progress")
    @Operation(summary = "Update KPI Progress", description = "Update achieved value for a KPI assignment")
    public ResponseEntity<KpiAssignmentResponse> updateProgress(
            @Valid @RequestBody KpiProgressUpdateRequest request) {
        
        KpiAssignmentResponse response = assignmentService.updateProgress(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/bulk-update-progress")
    @Operation(summary = "Bulk Update KPI Progress", description = "Update achieved values for multiple KPI assignments for an employee")
    public ResponseEntity<List<KpiAssignmentResponse>> bulkUpdateProgress(
            @Valid @RequestBody KpiBulkProgressUpdateRequest request) {
        
        List<KpiAssignmentResponse> response = assignmentService.bulkUpdateProgress(request);
        return ResponseEntity.ok(response);
    }

    // ============ View Results ============

    @GetMapping("/results/{employeeId}")
    @Operation(summary = "My KPI Results", description = "Get all KPI results for an employee")
    public ResponseEntity<List<KpiResultResponse>> getMyResults(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        List<KpiResultResponse> response = resultService.getResultsByEmployee(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results-with-grades/{employeeId}")
    @Operation(summary = "My KPI Results with Grades", description = "Get all KPI assignments with grades for an employee")
    public ResponseEntity<List<KpiAssignmentWithGradeResponse>> getMyResultsWithGrades(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        List<KpiAssignmentWithGradeResponse> response = assignmentService.getAssignmentsWithGrades(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/{employeeId}/year/{year}")
    @Operation(summary = "My KPI Results by Year", description = "Get KPI results for a specific year")
    public ResponseEntity<List<KpiResultResponse>> getMyResultsByYear(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        List<KpiResultResponse> response = resultService.getResultsByEmployeeAndYear(employeeId, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/{employeeId}/month/{month}/year/{year}")
    @Operation(summary = "My KPI Result for Month", description = "Get KPI result for a specific month")
    public ResponseEntity<KpiResultResponse> getMyResultForMonth(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Month (1-12)") @PathVariable Integer month,
            @Parameter(description = "Year") @PathVariable Integer year) {
        
        KpiResultResponse response = resultService.getResultByEmployeeMonthYear(employeeId, month, year);
        return ResponseEntity.ok(response);
    }

}
