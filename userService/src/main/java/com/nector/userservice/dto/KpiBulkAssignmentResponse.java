package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiBulkAssignmentResponse {

    private Long employeeId;
    private String employeeName;
    private String designation;
    private String roleName;

    // Weightage summary
    private Integer totalWeightageAssigned;
    private Integer totalWeightageUsed;
    private Integer remainingWeightage;
    private String weightageStatus; // COMPLETE, PARTIAL, EXCEEDED

    private List<KpiAssignmentResponse> assignments;
    private String message;
}
