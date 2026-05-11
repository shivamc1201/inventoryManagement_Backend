package com.nector.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiWeightageValidationRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotEmpty(message = "At least one KPI assignment is required")
    @Valid
    private List<KpiWeightageItem> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiWeightageItem {
        private Long kpiId;
        private Integer weightage;
    }
}
