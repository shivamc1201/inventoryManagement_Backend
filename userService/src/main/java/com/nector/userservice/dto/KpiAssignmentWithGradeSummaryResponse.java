package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiAssignmentWithGradeSummaryResponse {

    private List<KpiAssignmentWithGradeResponse> kpis;

    // Current month snapshot (live-calculated from active assignments)
    private Integer month;
    private Integer year;
    private BigDecimal totalScore;
    private KPIGrade finalGrade;
    private String finalGradeMeaning;

    // Backward-compat fields
    private BigDecimal totalWeightedScore;
    private String overallGrade;
    private String overallGradeMeaning;
}
