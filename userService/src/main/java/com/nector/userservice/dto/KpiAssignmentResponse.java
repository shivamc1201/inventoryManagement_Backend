package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiAssignmentResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String designation;
    private String roleName;
    private Long kpiId;
    private String kpiCode;
    private String kpiName;
    private KpiMasterResponse kpiMaster;
    private BigDecimal targetValue;
    private BigDecimal achievedValue;
    private Integer weightage;
    private BigDecimal scorePercentage;
    private BigDecimal weightedScore;
    private LocalDate startDate;
    private LocalDate endDate;
    private KPIStatus status;
    private String remarks;
    private Long assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
