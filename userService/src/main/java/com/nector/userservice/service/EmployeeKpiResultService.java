package com.nector.userservice.service;

import com.nector.userservice.dto.KpiReportFilterRequest;
import com.nector.userservice.dto.KpiResultResponse;
import com.nector.userservice.enums.KPIGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeKpiResultService {

    KpiResultResponse generateResult(Long employeeId, Integer month, Integer year);

    KpiResultResponse generateResultForCurrentMonth(Long employeeId);

    List<KpiResultResponse> generateResultsForAllEmployees(Integer month, Integer year);

    KpiResultResponse getResultById(Long id);

    KpiResultResponse getResultByEmployeeMonthYear(Long employeeId, Integer month, Integer year);

    List<KpiResultResponse> getResultsByEmployee(Long employeeId);

    List<KpiResultResponse> getResultsByEmployeeAndYear(Long employeeId, Integer year);

    List<KpiResultResponse> getResultsByMonthAndYear(Integer month, Integer year);

    List<KpiResultResponse> getResultsByYear(Integer year);

    Page<KpiResultResponse> getResultsByFilters(KpiReportFilterRequest filter, Pageable pageable);

    List<KpiResultResponse> getTopPerformers(Integer month, Integer year, int limit);

    List<KpiResultResponse> getTopPerformersByYear(Integer year, int limit);
}
