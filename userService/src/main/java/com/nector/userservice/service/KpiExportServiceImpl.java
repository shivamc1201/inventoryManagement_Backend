package com.nector.userservice.service;

import com.nector.userservice.dto.KpiReportFilterRequest;
import com.nector.userservice.util.KpiExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiExportServiceImpl implements KpiExportService {

    private final EmployeeKpiAssignmentService assignmentService;
    private final EmployeeKpiResultService resultService;
    private final KpiExportUtil exportUtil;

    @Override
    public byte[] exportAssignmentsToExcel(KpiReportFilterRequest filter) {
        log.info("Exporting assignments to Excel with filter");
        
        try {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
            var assignments = assignmentService.getAllAssignments(filter, pageable);
            
            return exportUtil.exportAssignmentsToExcel(assignments.getContent());
        } catch (Exception e) {
            log.error("Error exporting assignments to Excel", e);
            throw new RuntimeException("Failed to export assignments to Excel", e);
        }
    }

    @Override
    public byte[] exportResultsToExcel(KpiReportFilterRequest filter) {
        log.info("Exporting results to Excel with filter");
        
        try {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by("totalScore").descending());
            var results = resultService.getResultsByFilters(filter, pageable);
            
            return exportUtil.exportResultsToExcel(results.getContent());
        } catch (Exception e) {
            log.error("Error exporting results to Excel", e);
            throw new RuntimeException("Failed to export results to Excel", e);
        }
    }

    @Override
    public byte[] exportAssignmentsToPdf(KpiReportFilterRequest filter) {
        log.info("Exporting assignments to PDF with filter");
        
        try {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
            var assignments = assignmentService.getAllAssignments(filter, pageable);
            
            return exportUtil.exportAssignmentsToPdf(assignments.getContent());
        } catch (Exception e) {
            log.error("Error exporting assignments to PDF", e);
            throw new RuntimeException("Failed to export assignments to PDF", e);
        }
    }

    @Override
    public byte[] exportResultsToPdf(KpiReportFilterRequest filter) {
        log.info("Exporting results to PDF with filter");
        
        try {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by("totalScore").descending());
            var results = resultService.getResultsByFilters(filter, pageable);
            
            return exportUtil.exportResultsToPdf(results.getContent());
        } catch (Exception e) {
            log.error("Error exporting results to PDF", e);
            throw new RuntimeException("Failed to export results to PDF", e);
        }
    }
}
