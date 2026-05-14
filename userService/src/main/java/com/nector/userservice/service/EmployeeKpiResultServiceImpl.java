package com.nector.userservice.service;

import com.nector.userservice.dto.KpiReportFilterRequest;
import com.nector.userservice.dto.KpiResultResponse;
import com.nector.userservice.dto.KpiYearlySummaryResponse;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.enums.KPIGrade;
import com.nector.userservice.exception.KpiNotFoundException;
import com.nector.userservice.model.EmployeeKpiAssignment;
import com.nector.userservice.model.EmployeeKpiResult;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.EmployeeKpiResultRepository;
import com.nector.userservice.util.KpiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeKpiResultServiceImpl implements EmployeeKpiResultService {

    private final EmployeeKpiResultRepository resultRepository;
    private final EmployeeKpiAssignmentRepository assignmentRepository;
    private final KpiCalculator kpiCalculator;

    @Override
    @Transactional
    public KpiResultResponse generateResult(Long employeeId, Integer month, Integer year) {
        log.info("Generating KPI result for employee {} for {}/{}", employeeId, month, year);
        
        // Calculate date range for the month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // Get all assignments for the employee within the date range
        List<EmployeeKpiAssignment> assignments = assignmentRepository
                .findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        
        // Calculate total weighted score
        BigDecimal totalScore = kpiCalculator.calculateTotalWeightedScore(assignments);
        
        // Calculate grade
        KPIGrade grade = kpiCalculator.calculateGrade(totalScore);
        
        // Check if result already exists
        EmployeeKpiResult result = resultRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElse(new EmployeeKpiResult());
        
        result.setEmployeeId(employeeId);
        result.setMonth(month);
        result.setYear(year);
        result.setTotalScore(totalScore);
        result.setFinalGrade(grade);
        result.setGradeMeaning(grade.getMeaning());
        
        EmployeeKpiResult saved = resultRepository.save(result);
        log.info("Generated KPI result ID: {} for employee {} with score: {} and grade: {}", 
                saved.getId(), employeeId, totalScore, grade.getGrade());
        
        return mapToResponse(saved, getEmployeeName(assignments, employeeId));
    }

    @Override
    @Transactional
    public KpiResultResponse generateResultForCurrentMonth(Long employeeId) {
        LocalDate now = LocalDate.now();
        return generateResult(employeeId, now.getMonthValue(), now.getYear());
    }

    @Override
    @Transactional
    public List<KpiResultResponse> generateResultsForAllEmployees(Integer month, Integer year) {
        log.info("Generating KPI results for all employees for {}/{}", month, year);
        
        // Get all distinct employee IDs with active assignments in the period
        List<Long> employeeIds = assignmentRepository
                .findDistinctEmployeeIdsByStatus(KPIStatus.ACTIVE);
        
        return employeeIds.stream()
                .map(empId -> generateResult(empId, month, year))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KpiResultResponse getResultById(Long id) {
        EmployeeKpiResult result = resultRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException("KPI result not found with ID: " + id));
        
        String employeeName = getEmployeeNameById(result.getEmployeeId());
        return mapToResponse(result, employeeName);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiResultResponse getResultByEmployeeMonthYear(Long employeeId, Integer month, Integer year) {
        EmployeeKpiResult result = resultRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new KpiNotFoundException(
                        "KPI result not found for employee " + employeeId + " for " + month + "/" + year));
        
        String employeeName = getEmployeeNameById(employeeId);
        return mapToResponse(result, employeeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getResultsByEmployee(Long employeeId) {
        return resultRepository.findByEmployeeId(employeeId)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(employeeId)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getResultsByEmployeeAndYear(Long employeeId, Integer year) {
        List<KpiResultResponse> storedResults = resultRepository.findByEmployeeIdAndYearOrderByMonthDesc(employeeId, year)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(employeeId)))
                .collect(Collectors.toList());

        // If we are querying the current year, check if the current month snapshot exists.
        // If not, calculate it live from active assignments so the caller always sees the current month.
        LocalDate today = LocalDate.now();
        if (year == today.getYear()) {
            int currentMonth = today.getMonthValue();
            boolean currentMonthPresent = storedResults.stream()
                    .anyMatch(r -> r.getMonth() != null && r.getMonth() == currentMonth);

            if (!currentMonthPresent) {
                // Build a live (unsaved) snapshot for the current month
                LocalDate monthStart = LocalDate.of(year, currentMonth, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                List<EmployeeKpiAssignment> assignments = assignmentRepository
                        .findByEmployeeIdAndDateRange(employeeId, monthStart, monthEnd);

                BigDecimal liveScore = kpiCalculator.calculateTotalWeightedScore(assignments);
                KPIGrade liveGrade  = kpiCalculator.calculateGrade(liveScore);
                String empName      = getEmployeeName(assignments, employeeId);

                KpiResultResponse liveResult = KpiResultResponse.builder()
                        .id(null)           // not persisted yet
                        .employeeId(employeeId)
                        .employeeName(empName)
                        .month(currentMonth)
                        .year(year)
                        .totalScore(liveScore)
                        .finalGrade(liveGrade)
                        .gradeMeaning(liveGrade.getMeaning())
                        .generatedAt(null)  // live — not generated/saved
                        .build();

                // Insert at top (most recent first)
                storedResults = new java.util.ArrayList<>(storedResults);
                storedResults.add(0, liveResult);
            }
        }

        return storedResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getResultsByMonthAndYear(Integer month, Integer year) {
        return resultRepository.findByMonthAndYear(month, year)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(r.getEmployeeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getResultsByYear(Integer year) {
        return resultRepository.findByYear(year)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(r.getEmployeeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiResultResponse> getResultsByFilters(KpiReportFilterRequest filter, Pageable pageable) {
        Page<EmployeeKpiResult> results = resultRepository.findByFilters(
                filter.getEmployeeId(),
                filter.getMonth(),
                filter.getYear(),
                null, // grade filter not implemented in repo yet
                pageable
        );
        
        return results.map(r -> mapToResponse(r, getEmployeeNameById(r.getEmployeeId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getTopPerformers(Integer month, Integer year, int limit) {
        Pageable topN = PageRequest.of(0, limit, Sort.by("totalScore").descending());
        return resultRepository.findTopPerformersByMonthAndYear(month, year, topN)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(r.getEmployeeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiResultResponse> getTopPerformersByYear(Integer year, int limit) {
        Pageable topN = PageRequest.of(0, limit, Sort.by("totalScore").descending());
        return resultRepository.findTopPerformersByYear(year, topN)
                .stream()
                .map(r -> mapToResponse(r, getEmployeeNameById(r.getEmployeeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KpiYearlySummaryResponse getYearlySummary(Long employeeId, Integer year) {
        log.info("Generating yearly KPI summary for employee {} for year {}", employeeId, year);

        List<KpiResultResponse> monthlyResults = getResultsByEmployeeAndYear(employeeId, year);

        if (monthlyResults.isEmpty()) {
            String empName = getEmployeeNameById(employeeId);
            return KpiYearlySummaryResponse.builder()
                    .employeeId(employeeId)
                    .employeeName(empName)
                    .year(year)
                    .monthlyResults(List.of())
                    .monthsRecorded(0)
                    .averageMonthlyScore(BigDecimal.ZERO)
                    .yearlyGrade(KPIGrade.fromScore(0))
                    .yearlyGradeMeaning(KPIGrade.fromScore(0).getMeaning())
                    .bestMonthScore(BigDecimal.ZERO)
                    .worstMonthScore(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal totalScore = monthlyResults.stream()
                .map(r -> r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgScore = totalScore.divide(
                java.math.BigDecimal.valueOf(monthlyResults.size()), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal bestScore = monthlyResults.stream()
                .map(r -> r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal worstScore = monthlyResults.stream()
                .map(r -> r.getTotalScore() != null ? r.getTotalScore() : BigDecimal.ZERO)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        KPIGrade yearlyGrade = KPIGrade.fromScore(avgScore.doubleValue());
        String empName = monthlyResults.get(0).getEmployeeName();

        return KpiYearlySummaryResponse.builder()
                .employeeId(employeeId)
                .employeeName(empName)
                .year(year)
                .monthlyResults(monthlyResults)
                .monthsRecorded(monthlyResults.size())
                .averageMonthlyScore(avgScore)
                .yearlyGrade(yearlyGrade)
                .yearlyGradeMeaning(yearlyGrade.getMeaning())
                .bestMonthScore(bestScore)
                .worstMonthScore(worstScore)
                .build();
    }

    private String getEmployeeNameById(Long employeeId) {
        // Get employee name from the most recent assignment
        List<EmployeeKpiAssignment> assignments = assignmentRepository.findByEmployeeId(employeeId);
        return getEmployeeName(assignments, employeeId);
    }

    private String getEmployeeName(List<EmployeeKpiAssignment> assignments, Long employeeId) {
        return assignments.stream()
                .findFirst()
                .map(EmployeeKpiAssignment::getEmployeeName)
                .orElse("Employee " + employeeId);
    }

    private KpiResultResponse mapToResponse(EmployeeKpiResult result, String employeeName) {
        return KpiResultResponse.builder()
                .id(result.getId())
                .employeeId(result.getEmployeeId())
                .employeeName(employeeName)
                .month(result.getMonth())
                .year(result.getYear())
                .totalScore(result.getTotalScore())
                .finalGrade(result.getFinalGrade())
                .gradeMeaning(result.getGradeMeaning())
                .generatedAt(result.getGeneratedAt())
                .build();
    }
}
