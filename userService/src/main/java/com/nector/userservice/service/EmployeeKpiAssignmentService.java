package com.nector.userservice.service;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPIStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeKpiAssignmentService {

    KpiAssignmentResponse assignKpiToEmployee(KpiAssignmentRequest request, Long assignedBy);

    KpiBulkAssignmentResponse bulkAssignKpisToEmployee(KpiBulkAssignmentRequest request, Long assignedBy);

    KpiAssignmentResponse updateAssignment(Long id, KpiAssignmentRequest request);

    KpiAssignmentResponse updateProgress(KpiProgressUpdateRequest request);

    void deleteAssignment(Long id);

    KpiAssignmentResponse getAssignmentById(Long id);

    List<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId);

    Page<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId, Pageable pageable);

    List<KpiAssignmentResponse> getAssignmentsByEmployeeAndStatus(Long employeeId, KPIStatus status);

    List<KpiAssignmentResponse> getCurrentActiveAssignments(Long employeeId);

    Page<KpiAssignmentResponse> getAllAssignments(KpiReportFilterRequest filter, Pageable pageable);

    EmployeeKpiDashboardResponse getEmployeeDashboard(Long employeeId);

    void expireOldAssignments();

    Integer getTotalWeightageForEmployee(Long employeeId);
}
