package com.nector.userservice.dto;

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
    private BigDecimal totalWeightedScore;
    private String overallGrade;
    private String overallGradeMeaning;
}

