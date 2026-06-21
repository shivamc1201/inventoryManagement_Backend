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
public class EmployeeKpiDashboardResponse {

    private Long employeeId;
    private String employeeName;
    private String designation;
    private String roleName;
    private List<KpiAssignmentResponse> assignedKpis;
    private BigDecimal totalTargetScore;
    private BigDecimal totalAchievedScore;
    private BigDecimal overallProgressPercentage;
    private BigDecimal projectedFinalScore;
    private KPIGrade projectedGrade;
    private String projectedGradeMeaning;
    private Integer activeKpiCount;
    private Integer completedKpiCount;
    private Integer expiredKpiCount;
}
