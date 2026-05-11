package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiReportResponse {

    private List<KpiAssignmentResponse> assignments;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private Long employeeId;
    private String employeeName;
    private Integer month;
    private Integer year;
    private BigDecimal totalWeightedScore;
    private KPIGrade overallGrade;
    private String overallGradeMeaning;
}
