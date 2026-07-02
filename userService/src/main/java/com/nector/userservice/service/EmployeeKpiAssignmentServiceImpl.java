package com.nector.userservice.service;

import com.nector.userservice.dto.*;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.exception.KpiAssignmentException;
import com.nector.userservice.exception.KpiNotFoundException;
import com.nector.userservice.model.EmployeeKpiAssignment;
import com.nector.userservice.model.KpiMaster;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.KpiMasterRepository;
import com.nector.userservice.util.KpiCalculator;
import com.nector.userservice.util.KpiGradeCalculator;
import com.nector.userservice.validator.KpiValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeKpiAssignmentServiceImpl implements EmployeeKpiAssignmentService {

    private final EmployeeKpiAssignmentRepository assignmentRepository;
    private final KpiMasterRepository kpiMasterRepository;
    private final KpiValidator kpiValidator;
    private final KpiCalculator kpiCalculator;
    private final EmployeeKpiResultService resultService;

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

        LocalDate effectiveStartDate = request.getEffectiveStartDate();
        LocalDate effectiveEndDate = request.getEffectiveEndDate();

        log.info("Assigning KPIs for period: {} to {}", effectiveStartDate, effectiveEndDate);
        
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
        if (effectiveEndDate.isBefore(effectiveStartDate)) {
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
                    effectiveStartDate,
                    effectiveEndDate
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
            assignment.setStartDate(effectiveStartDate);
            assignment.setEndDate(effectiveEndDate);
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
    public List<KpiAssignmentResponse> bulkUpdateProgress(KpiBulkProgressUpdateRequest request) {
        log.info("=== BULK UPDATE PROGRESS START ===");
        log.info("Bulk updating progress for employee ID: {} with {} assignments", 
                request.getEmployeeId(), request.getAssignments().size());
        
        List<KpiAssignmentResponse> responses = new ArrayList<>();
        
        for (KpiBulkProgressUpdateRequest.KpiProgressUpdateItem item : request.getAssignments()) {
            log.info("--- Processing assignment ID: {} ---", item.getAssignmentId());
            log.info("Request achieved value: {}", item.getAchievedValue());
            
            EmployeeKpiAssignment assignment = assignmentRepository.findById(item.getAssignmentId())
                    .orElseThrow(() -> new KpiNotFoundException("Assignment not found with ID: " + item.getAssignmentId()));
            
            // Validate that the assignment belongs to the specified employee
            if (!assignment.getEmployeeId().equals(request.getEmployeeId())) {
                throw new BusinessException("Assignment ID " + item.getAssignmentId() + 
                        " does not belong to employee ID " + request.getEmployeeId());
            }
            
            // Update achieved value and recalculate
            assignment.updateAchievedValue(item.getAchievedValue());
            
            EmployeeKpiAssignment updated = assignmentRepository.save(assignment);
            responses.add(mapToResponse(updated));
            
            log.info("Updated progress for assignment ID: {}. New status: {}, Score: {}%", 
                    updated.getId(), updated.getStatus(), updated.getScorePercentage());
        }
        
        log.info("Successfully updated {} assignments for employee ID: {}", 
                responses.size(), request.getEmployeeId());
        
        return responses;
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

    private static final Map<Long, Integer> KPI_DISPLAY_ORDER = Map.of(
        15L, 0,  // Farmer Visit
        17L, 1,  // Dealer Visit
        16L, 2,  // Distributor Visit
        20L, 3,  // Retailer Visit
        18L, 4,  // Dealer Onboard
        19L, 5,  // Distributor Onboard
        14L, 6   // Sale Target
    );

    @Override
    @Transactional(readOnly = true)
    public List<KpiAssignmentResponse> getAllAssignments() {
        YearMonth currentMonth = YearMonth.now();
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .filter(r -> r.getStartDate() != null &&
                        YearMonth.from(r.getStartDate()).equals(currentMonth))
                .sorted(Comparator
                        .comparing(KpiAssignmentResponse::getEmployeeId,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(r ->
                                KPI_DISPLAY_ORDER.getOrDefault(r.getKpiId(), Integer.MAX_VALUE)))
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
    public void rolloverAssignmentsToNextMonth() {
        LocalDate today = LocalDate.now();
        LocalDate prev = today.minusMonths(1);

        int prevMonth = prev.getMonthValue();
        int prevYear  = prev.getYear();
        int newMonth  = today.getMonthValue();
        int newYear   = today.getYear();

        LocalDate newStart = LocalDate.of(newYear, newMonth, 1);
        LocalDate newEnd   = newStart.withDayOfMonth(newStart.lengthOfMonth());

        log.info("Rolling over KPI assignments from {}/{} → {}/{}", prevMonth, prevYear, newMonth, newYear);

        List<EmployeeKpiAssignment> previous = assignmentRepository.findAllByMonthAndYear(prevMonth, prevYear);
        int created = 0, skipped = 0;

        for (EmployeeKpiAssignment src : previous) {
            boolean exists = assignmentRepository
                    .findOverlappingByEmployeeAndKpi(src.getEmployeeId(), src.getKpiId(),
                            KPIStatus.ACTIVE, newStart, newEnd)
                    .isPresent();
            if (exists) { skipped++; continue; }

            EmployeeKpiAssignment next = new EmployeeKpiAssignment();
            next.setEmployeeId(src.getEmployeeId());
            next.setEmployeeName(src.getEmployeeName());
            next.setDesignation(src.getDesignation());
            next.setRoleName(src.getRoleName());
            next.setKpiId(src.getKpiId());
            next.setTargetValue(src.getTargetValue());
            next.setAchievedValue(BigDecimal.ZERO);
            next.setWeightage(src.getWeightage());
            next.setScorePercentage(BigDecimal.ZERO);
            next.setWeightedScore(BigDecimal.ZERO);
            next.setStartDate(newStart);
            next.setEndDate(newEnd);
            next.setStatus(KPIStatus.ACTIVE);
            next.setRemarks(src.getRemarks());
            next.setAssignedBy(src.getAssignedBy());
            assignmentRepository.save(next);
            created++;
        }

        log.info("Rollover complete: {} assignments created, {} skipped for {}/{}",
                created, skipped, newMonth, newYear);
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

    @Override
    @Transactional
    public void autoUpdateKpiAchieved(Long employeeId, String kpiName, BigDecimal incrementAmount) {
        if (employeeId == null || kpiName == null || incrementAmount == null) {
            log.warn("autoUpdateKpiAchieved called with null parameters: employeeId={}, kpiName={}, incrementAmount={}",
                    employeeId, kpiName, incrementAmount);
            return;
        }
        List<EmployeeKpiAssignment> assignments =
                assignmentRepository.findActiveByEmployeeIdAndKpiName(employeeId, kpiName);
        if (assignments.isEmpty()) {
            log.info("No active KPI assignment found for employee {} with kpiName '{}'", employeeId, kpiName);
            return;
        }
        for (EmployeeKpiAssignment assignment : assignments) {
            BigDecimal current = assignment.getAchievedValue() != null ? assignment.getAchievedValue() : BigDecimal.ZERO;
            BigDecimal newValue = current.add(incrementAmount);
            log.info("Auto-updating KPI '{}' (assignmentId={}) for employee {}: {} -> {}",
                    kpiName, assignment.getId(), employeeId, current, newValue);
            assignment.updateAchievedValue(newValue);
            assignmentRepository.save(assignment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KpiAssignmentWithGradeSummaryResponse getAssignmentsWithGrades(Long employeeId) {
        log.info("Getting assignments with grades for employee ID: {}", employeeId);

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear  = LocalDate.now().getYear();

        // Only return assignments for the current month
        List<EmployeeKpiAssignment> assignments =
                assignmentRepository.findByEmployeeIdAndMonthAndYear(employeeId, currentMonth, currentYear);

        List<KpiAssignmentWithGradeResponse> kpiResponses = assignments.stream()
                .map(this::mapToResponseWithGrade)
                .collect(Collectors.toList());

        BigDecimal totalWeightedScore = kpiResponses.stream()
                .map(r -> r.getWeightedScore() != null ? r.getWeightedScore() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        KpiGradeCalculator.GradeResult overallGrade = KpiGradeCalculator.calculateGrade(totalWeightedScore);

        // Derive KPIGrade enum from the string grade for the finalGrade field
        com.nector.userservice.enums.KPIGrade finalGrade =
                com.nector.userservice.enums.KPIGrade.fromScore(totalWeightedScore.doubleValue());

        // Monthly history + yearly aggregate for the current year
        java.util.List<com.nector.userservice.dto.KpiResultResponse> monthlyHistory =
                resultService.getResultsByEmployeeAndYear(employeeId, currentYear);

        com.nector.userservice.dto.KpiYearlySummaryResponse yearly =
                resultService.getYearlySummary(employeeId, currentYear);

        return KpiAssignmentWithGradeSummaryResponse.builder()
                .kpis(kpiResponses)
                // current month snapshot fields
                .month(currentMonth)
                .year(currentYear)
                .totalScore(totalWeightedScore)
                .finalGrade(finalGrade)
                .finalGradeMeaning(finalGrade.getMeaning())
                // monthly history and yearly aggregate
                .monthlyHistory(monthlyHistory)
                .yearlyAverage(yearly.getAverageMonthlyScore())
                .yearlyGrade(yearly.getYearlyGrade())
                .yearlyGradeMeaning(yearly.getYearlyGradeMeaning())
                // backward-compat fields
                .totalWeightedScore(totalWeightedScore)
                .overallGrade(overallGrade.getGrade())
                .overallGradeMeaning(overallGrade.getMeaning())
                .build();
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

    private KpiAssignmentWithGradeResponse mapToResponseWithGrade(EmployeeKpiAssignment assignment) {
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
                    .description(kpi.getDescription())
                    .kpiCategory(kpi.getKpiCategory())
                    .measurementUnit(kpi.getMeasurementUnit())
                    .defaultWeightage(kpi.getDefaultWeightage())
                    .frequency(kpi.getFrequency())
                    .isActive(kpi.getIsActive())
                    .createdBy(kpi.getCreatedBy())
                    .createdAt(kpi.getCreatedAt())
                    .updatedAt(kpi.getUpdatedAt())
                    .build();
        }

        // Calculate grade and meaning
        KpiGradeCalculator.GradeResult gradeResult = KpiGradeCalculator.calculateGrade(assignment.getScorePercentage());

        return KpiAssignmentWithGradeResponse.builder()
                .id(assignment.getId())
                .employeeId(assignment.getEmployeeId())
                .employeeName(assignment.getEmployeeName())
                .designation(assignment.getDesignation())
                .roleName(assignment.getRoleName())
                .kpiId(assignment.getKpiId())
                .kpiCode(kpi != null ? kpi.getKpiCode() : null)
                .kpiName(kpi != null ? kpi.getKpiName() : null)
                .kpiMaster(kpi)
                .targetValue(assignment.getTargetValue())
                .achievedValue(assignment.getAchievedValue())
                .weightage(assignment.getWeightage())
                .scorePercentage(assignment.getScorePercentage())
                .weightedScore(assignment.getWeightedScore())
                .startDate(assignment.getStartDate() != null ? assignment.getStartDate().toString() : null)
                .endDate(assignment.getEndDate() != null ? assignment.getEndDate().toString() : null)
                .status(assignment.getStatus() != null ? assignment.getStatus().toString() : null)
                .remarks(assignment.getRemarks())
                .assignedBy(assignment.getAssignedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .grade(gradeResult.getGrade())
                .gradeMeaning(gradeResult.getMeaning())
                .build();
    }
}
