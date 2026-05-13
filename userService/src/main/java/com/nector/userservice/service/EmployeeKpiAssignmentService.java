package com.nector.userservice.service;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPIStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeKpiAssignmentService {

    KpiAssignmentResponse assignKpiToEmployee(KpiAssignmentRequest request, Long assignedBy);

    KpiBulkAssignmentResponse bulkAssignKpisToEmployee(KpiBulkAssignmentRequest request, Long assignedBy);

    KpiAssignmentResponse updateAssignment(Long id, KpiAssignmentRequest request);

    KpiAssignmentResponse updateProgress(KpiProgressUpdateRequest request);

    List<KpiAssignmentResponse> bulkUpdateProgress(KpiBulkProgressUpdateRequest request);

    void deleteAssignment(Long id);

    KpiAssignmentResponse getAssignmentById(Long id);

    List<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId);

    Page<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId, Pageable pageable);

    List<KpiAssignmentResponse> getAssignmentsByEmployeeAndStatus(Long employeeId, KPIStatus status);

    List<KpiAssignmentResponse> getCurrentActiveAssignments(Long employeeId);

    List<KpiAssignmentWithGradeResponse> getAssignmentsWithGrades(Long employeeId);

    Page<KpiAssignmentResponse> getAllAssignments(KpiReportFilterRequest filter, Pageable pageable);

    List<KpiAssignmentResponse> getAllAssignments();

    EmployeeKpiDashboardResponse getEmployeeDashboard(Long employeeId);

    void expireOldAssignments();

    Integer getTotalWeightageForEmployee(Long employeeId);

    /**
     * Auto-update the achieved value for a salesperson's active KPI by EXACT kpi name.
     * Exact names in use: "Sale Target", "Dealer Onboard", "Distributor Onboard"
     *
     * @param employeeId      the salesperson's employee ID
     * @param kpiName         exact KPI name as stored in kpi_master.kpi_name
     * @param incrementAmount the amount to ADD to the current achieved value
     */
    void autoUpdateKpiAchieved(Long employeeId, String kpiName, BigDecimal incrementAmount);
}
