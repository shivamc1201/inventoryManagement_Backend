package com.nector.userservice.dto;

import com.nector.userservice.model.KpiMaster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiAssignmentWithGradeResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String designation;
    private String roleName;
    private Long kpiId;
    private String kpiCode;
    private String kpiName;
    private KpiMaster kpiMaster;
    private BigDecimal targetValue;
    private BigDecimal achievedValue;
    private Integer weightage;
    private BigDecimal scorePercentage;
    private BigDecimal weightedScore;
    private String startDate;
    private String endDate;
    private String status;
    private String remarks;
    private Long assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String grade;
    private String gradeMeaning;
}
