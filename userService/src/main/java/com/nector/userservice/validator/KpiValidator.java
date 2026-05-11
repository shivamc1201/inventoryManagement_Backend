package com.nector.userservice.validator;

import com.nector.userservice.dto.KpiAssignmentRequest;
import com.nector.userservice.dto.KpiMasterCreateRequest;
import com.nector.userservice.exception.KpiAssignmentException;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.KpiMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class KpiValidator {

    private final KpiMasterRepository kpiMasterRepository;
    private final EmployeeKpiAssignmentRepository assignmentRepository;

    public void validateKpiMasterCreate(KpiMasterCreateRequest request) {
        if (request.getDefaultWeightage() != null && 
            (request.getDefaultWeightage() < 0 || request.getDefaultWeightage() > 100)) {
            throw new com.nector.userservice.exception.KpiException(
                "Default weightage must be between 0 and 100");
        }
    }

    public void validateKpiMasterUpdate(String kpiCode, Long id) {
        if (kpiCode != null && !kpiCode.isBlank() &&
            kpiMasterRepository.existsByKpiCodeAndIdNot(kpiCode, id)) {
            throw new com.nector.userservice.exception.KpiException(
                "KPI with code '" + kpiCode + "' already exists");
        }
    }

    public void validateAssignment(KpiAssignmentRequest request) {
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw KpiAssignmentException.invalidDateRange();
        }

        // Check if KPI exists
        if (!kpiMasterRepository.existsById(request.getKpiId())) {
            throw new com.nector.userservice.exception.KpiNotFoundException(request.getKpiId());
        }

        // Check for overlapping assignments for same KPI
        var overlapping = assignmentRepository.findOverlappingByEmployeeAndKpi(
            request.getEmployeeId(),
            request.getKpiId(),
            com.nector.userservice.enums.KPIStatus.ACTIVE,
            request.getStartDate(),
            request.getEndDate()
        );

        if (overlapping.isPresent()) {
            throw KpiAssignmentException.duplicateAssignment(request.getEmployeeId(), request.getKpiId());
        }

        // Validate weightage does not exceed 100%
        Integer currentWeightage = assignmentRepository.sumWeightageByEmployeeIdAndStatus(
            request.getEmployeeId(),
            com.nector.userservice.enums.KPIStatus.ACTIVE
        );

        if (currentWeightage == null) {
            currentWeightage = 0;
        }

        int totalWeightage = currentWeightage + request.getWeightage();
        if (totalWeightage > 100) {
            throw KpiAssignmentException.weightageExceeded(
                request.getEmployeeId(), currentWeightage, request.getWeightage());
        }
    }

    public void validateWeightageForUpdate(Long employeeId, Integer newWeightage, Long excludeAssignmentId) {
        Integer currentWeightage = assignmentRepository.sumWeightageByEmployeeIdAndStatus(
            employeeId,
            com.nector.userservice.enums.KPIStatus.ACTIVE
        );

        if (currentWeightage == null) {
            currentWeightage = 0;
        }

        // Get weightage of the assignment being updated
        var assignment = assignmentRepository.findById(excludeAssignmentId);
        int existingWeightage = assignment.map(a -> a.getWeightage()).orElse(0);

        int totalWeightage = currentWeightage - existingWeightage + newWeightage;
        if (totalWeightage > 100) {
            throw KpiAssignmentException.weightageExceeded(employeeId, currentWeightage - existingWeightage, newWeightage);
        }
    }
}
