package com.nector.userservice.service;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.exception.KpiAssignmentException;
import com.nector.userservice.exception.KpiNotFoundException;
import com.nector.userservice.model.EmployeeKpiAssignment;
import com.nector.userservice.model.KpiMaster;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.KpiMasterRepository;
import com.nector.userservice.util.KpiCalculator;
import com.nector.userservice.validator.KpiValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeKpiAssignmentServiceImpl implements EmployeeKpiAssignmentService {

    private final EmployeeKpiAssignmentRepository assignmentRepository;
    private final KpiMasterRepository kpiMasterRepository;
    private final KpiValidator kpiValidator;
    private final KpiCalculator kpiCalculator;

    @Override
    @Transactional
    public KpiAssignmentResponse assignKpiToEmployee(KpiAssignmentRequest request, Long assignedBy) {
        log.info("Assigning KPI {} to employee {} by user {}", 
                request.getKpiId(), request.getEmployeeId(), assignedBy);
        
        kpiValidator.validateAssignment(request);
        
        EmployeeKpiAssignment assignment = new EmployeeKpiAssignment();
        assignment.setEmployeeId(request.getEmployeeId());
        assignment.setEmployeeName(request.getEmployeeName());
        assignment.setDesignation(request.getDesignation());
        assignment.setRoleName(request.getRoleName());
        assignment.setKpiId(request.getKpiId());
        assignment.setTargetValue(request.getTargetValue());
        assignment.setAchievedValue(BigDecimal.ZERO);
        assignment.setWeightage(request.getWeightage());
        assignment.setScorePercentage(BigDecimal.ZERO);
        assignment.setWeightedScore(BigDecimal.ZERO);
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setStatus(KPIStatus.ACTIVE);
        assignment.setRemarks(request.getRemarks());
        assignment.setAssignedBy(assignedBy);
        
        // Calculate initial scores
        assignment.calculateScores();
        
        EmployeeKpiAssignment saved = assignmentRepository.save(assignment);
        log.info("Created KPI assignment with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public KpiBulkAssignmentResponse bulkAssignKpisToEmployee(KpiBulkAssignmentRequest request, Long assignedBy) {
        log.info("Bulk assigning {} KPIs to employee {} by user {}", 
                request.getAssignments().size(), request.getEmployeeId(), assignedBy);
        
        // Get current weightage for employee
        Integer currentWeightage = getTotalWeightageForEmployee(request.getEmployeeId());
        if (currentWeightage == null) {
            currentWeightage = 0;
        }
        
        // Calculate total weightage from new assignments
        int newWeightageTotal = request.getAssignments().stream()
                .mapToInt(KpiBulkAssignmentItem::getWeightage)
                .sum();
        
        int totalAfterAssignment = currentWeightage + newWeightageTotal;
        
        log.info("Current weightage: {}, New assignments: {}, Total after: {}", 
                currentWeightage, newWeightageTotal, totalAfterAssignment);
        
        // Validate total doesn't exceed 100
        if (totalAfterAssignment > 100) {
            throw new com.nector.userservice.exception.KpiException(
                String.format("Total weightage would exceed 100%%. Current: %d%%, Adding: %d%%, Would be: %d%%", 
                        currentWeightage, newWeightageTotal, totalAfterAssignment));
        }
        
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw KpiAssignmentException.invalidDateRange();
        }
        
        // Create assignments
        List<KpiAssignmentResponse> createdAssignments = new java.util.ArrayList<>();
        
        for (KpiBulkAssignmentItem item : request.getAssignments()) {
            // Validate KPI exists
            if (!kpiMasterRepository.existsById(item.getKpiId())) {
                throw new KpiNotFoundException(item.getKpiId());
            }
            
            // Check for overlapping assignments
            var overlapping = assignmentRepository.findOverlappingByEmployeeAndKpi(
                    request.getEmployeeId(),
                    item.getKpiId(),
                    KPIStatus.ACTIVE,
                    request.getStartDate(),
                    request.getEndDate()
            );
            
            if (overlapping.isPresent()) {
                throw KpiAssignmentException.duplicateAssignment(request.getEmployeeId(), item.getKpiId());
            }
            
            EmployeeKpiAssignment assignment = new EmployeeKpiAssignment();
            assignment.setEmployeeId(request.getEmployeeId());
            assignment.setEmployeeName(request.getEmployeeName());
            assignment.setDesignation(request.getDesignation());
            assignment.setRoleName(request.getRoleName());
            assignment.setKpiId(item.getKpiId());
            assignment.setTargetValue(item.getTargetValue());
            assignment.setAchievedValue(BigDecimal.ZERO);
            assignment.setWeightage(item.getWeightage());
            assignment.setScorePercentage(BigDecimal.ZERO);
            assignment.setWeightedScore(BigDecimal.ZERO);
            assignment.setStartDate(request.getStartDate());
            assignment.setEndDate(request.getEndDate());
            assignment.setStatus(KPIStatus.ACTIVE);
            assignment.setRemarks(request.getRemarks());
            assignment.setAssignedBy(assignedBy);
            
            assignment.calculateScores();
            
            EmployeeKpiAssignment saved = assignmentRepository.save(assignment);
            createdAssignments.add(mapToResponse(saved));
        }
        
        // Determine weightage status
        String weightageStatus;
        if (totalAfterAssignment == 100) {
            weightageStatus = "COMPLETE";
        } else if (totalAfterAssignment >= 80) {
            weightageStatus = "NEAR_COMPLETE";
        } else {
            weightageStatus = "PARTIAL";
        }
        
        log.info("Bulk assignment complete. Created {} assignments. Total weightage: {}/100", 
                createdAssignments.size(), totalAfterAssignment);
        
        return KpiBulkAssignmentResponse.builder()
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .designation(request.getDesignation())
                .roleName(request.getRoleName())
                .totalWeightageAssigned(newWeightageTotal)
                .totalWeightageUsed(totalAfterAssignment)
                .remainingWeightage(100 - totalAfterAssignment)
                .weightageStatus(weightageStatus)
                .assignments(createdAssignments)
                .message(String.format("Successfully assigned %d KPI(s). Weightage: %d%% used, %d%% remaining", 
                        createdAssignments.size(), totalAfterAssignment, 100 - totalAfterAssignment))
                .build();
    }

    @Override
    @Transactional
    public KpiAssignmentResponse updateAssignment(Long id, KpiAssignmentRequest request) {
        log.info("Updating assignment ID: {}", id);
        
        EmployeeKpiAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException("Assignment not found with ID: " + id));
        
        // Validate weightage for update
        if (!assignment.getWeightage().equals(request.getWeightage())) {
            kpiValidator.validateWeightageForUpdate(request.getEmployeeId(), request.getWeightage(), id);
        }
        
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw KpiAssignmentException.invalidDateRange();
        }
        
        assignment.setTargetValue(request.getTargetValue());
        assignment.setWeightage(request.getWeightage());
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setRemarks(request.getRemarks());
        
        // Recalculate scores
        assignment.calculateScores();
        
        EmployeeKpiAssignment updated = assignmentRepository.save(assignment);
        log.info("Updated assignment ID: {}", updated.getId());
        
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public KpiAssignmentResponse updateProgress(KpiProgressUpdateRequest request) {
        log.info("Updating progress for assignment ID: {} with achieved value: {}", 
                request.getAssignmentId(), request.getAchievedValue());
        
        EmployeeKpiAssignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new KpiNotFoundException("Assignment not found with ID: " + request.getAssignmentId()));
        
        // Update achieved value and recalculate
        assignment.updateAchievedValue(request.getAchievedValue());
        
        EmployeeKpiAssignment updated = assignmentRepository.save(assignment);
        log.info("Updated progress for assignment ID: {}. New status: {}, Score: {}%", 
                updated.getId(), updated.getStatus(), updated.getScorePercentage());
        
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        log.info("Deleting assignment ID: {}", id);
        
        EmployeeKpiAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException("Assignment not found with ID: " + id));
        
        assignmentRepository.delete(assignment);
        log.info("Deleted assignment ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiAssignmentResponse getAssignmentById(Long id) {
        EmployeeKpiAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException("Assignment not found with ID: " + id));
        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiAssignmentResponse> getAssignmentsByEmployee(Long employeeId, Pageable pageable) {
        return assignmentRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiAssignmentResponse> getAssignmentsByEmployeeAndStatus(Long employeeId, KPIStatus status) {
        return assignmentRepository.findByEmployeeIdAndStatus(employeeId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiAssignmentResponse> getCurrentActiveAssignments(Long employeeId) {
        return assignmentRepository.findCurrentActiveByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiAssignmentResponse> getAllAssignments(KpiReportFilterRequest filter, Pageable pageable) {
        Page<EmployeeKpiAssignment> assignments = assignmentRepository.findByFilters(
                filter.getEmployeeId(),
                filter.getKpiId(),
                filter.getStatus(),
                filter.getStartDateFrom(),
                filter.getStartDateTo(),
                filter.getEndDateFrom(),
                filter.getEndDateTo(),
                filter.getDesignation(),
                filter.getRoleName(),
                pageable
        );
        return assignments.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiAssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeKpiDashboardResponse getEmployeeDashboard(Long employeeId) {
        log.info("Generating dashboard for employee: {}", employeeId);
        
        List<EmployeeKpiAssignment> assignments = assignmentRepository.findCurrentActiveByEmployeeId(employeeId);
        
        if (assignments.isEmpty()) {
            return EmployeeKpiDashboardResponse.builder()
                    .employeeId(employeeId)
                    .assignedKpis(List.of())
                    .totalTargetScore(BigDecimal.ZERO)
                    .totalAchievedScore(BigDecimal.ZERO)
                    .overallProgressPercentage(BigDecimal.ZERO)
                    .projectedFinalScore(BigDecimal.ZERO)
                    .activeKpiCount(0)
                    .completedKpiCount(0)
                    .expiredKpiCount(0)
                    .build();
        }
        
        EmployeeKpiAssignment first = assignments.get(0);
        
        BigDecimal totalTarget = assignments.stream()
                .map(EmployeeKpiAssignment::getTargetValue)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAchieved = assignments.stream()
                .map(EmployeeKpiAssignment::getAchievedValue)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal overallProgress = kpiCalculator.calculateProgressPercentage(totalAchieved, totalTarget);
        BigDecimal projectedScore = kpiCalculator.calculateProjectedFinalScore(assignments);
        var projectedGrade = kpiCalculator.calculateGrade(projectedScore);
        
        long activeCount = assignments.stream().filter(a -> a.getStatus() == KPIStatus.ACTIVE).count();
        long completedCount = assignments.stream().filter(a -> a.getStatus() == KPIStatus.COMPLETED).count();
        long expiredCount = assignments.stream().filter(a -> a.getStatus() == KPIStatus.EXPIRED).count();
        
        return EmployeeKpiDashboardResponse.builder()
                .employeeId(employeeId)
                .employeeName(first.getEmployeeName())
                .designation(first.getDesignation())
                .roleName(first.getRoleName())
                .assignedKpis(assignments.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .totalTargetScore(totalTarget)
                .totalAchievedScore(totalAchieved)
                .overallProgressPercentage(overallProgress)
                .projectedFinalScore(projectedScore)
                .projectedGrade(projectedGrade)
                .projectedGradeMeaning(projectedGrade.getMeaning())
                .activeKpiCount((int) activeCount)
                .completedKpiCount((int) completedCount)
                .expiredKpiCount((int) expiredCount)
                .build();
    }

    @Override
    @Transactional
    public void expireOldAssignments() {
        log.info("Checking for expired assignments");
        
        List<EmployeeKpiAssignment> expired = assignmentRepository.findExpiredAssignments();
        
        for (EmployeeKpiAssignment assignment : expired) {
            assignment.setStatus(KPIStatus.EXPIRED);
            log.info("Marked assignment ID {} as EXPIRED", assignment.getId());
        }
        
        assignmentRepository.saveAll(expired);
        log.info("Expired {} assignments", expired.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalWeightageForEmployee(Long employeeId) {
        Integer weightage = assignmentRepository.sumWeightageByEmployeeIdAndStatus(employeeId, KPIStatus.ACTIVE);
        return weightage != null ? weightage : 0;
    }

    private KpiAssignmentResponse mapToResponse(EmployeeKpiAssignment assignment) {
        // Try to get from loaded entity, otherwise fetch from repository
        KpiMaster kpi = assignment.getKpiMaster();
        if (kpi == null && assignment.getKpiId() != null) {
            kpi = kpiMasterRepository.findById(assignment.getKpiId()).orElse(null);
        }
        
        KpiMasterResponse kpiMasterResponse = null;
        if (kpi != null) {
            kpiMasterResponse = KpiMasterResponse.builder()
                    .id(kpi.getId())
                    .kpiCode(kpi.getKpiCode())
                    .kpiName(kpi.getKpiName())
                    .kpiCategory(kpi.getKpiCategory())
                    .measurementUnit(kpi.getMeasurementUnit())
                    .frequency(kpi.getFrequency())
                    .build();
        }
        
        return KpiAssignmentResponse.builder()
                .id(assignment.getId())
                .employeeId(assignment.getEmployeeId())
                .employeeName(assignment.getEmployeeName())
                .designation(assignment.getDesignation())
                .roleName(assignment.getRoleName())
                .kpiId(assignment.getKpiId())
                .kpiCode(kpi != null ? kpi.getKpiCode() : null)
                .kpiName(kpi != null ? kpi.getKpiName() : null)
                .kpiMaster(kpiMasterResponse)
                .targetValue(assignment.getTargetValue())
                .achievedValue(assignment.getAchievedValue())
                .weightage(assignment.getWeightage())
                .scorePercentage(assignment.getScorePercentage())
                .weightedScore(assignment.getWeightedScore())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .status(assignment.getStatus())
                .remarks(assignment.getRemarks())
                .assignedBy(assignment.getAssignedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
