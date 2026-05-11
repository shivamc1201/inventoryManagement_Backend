package com.nector.userservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiBulkAssignmentItem {

    @NotNull(message = "KPI ID is required")
    private Long kpiId;

    @NotNull(message = "Target value is required")
    @Min(value = 0, message = "Target value must be at least 0")
    private BigDecimal targetValue;

    @NotNull(message = "Weightage is required")
    @Min(value = 0, message = "Weightage must be at least 0")
    @Max(value = 100, message = "Weightage cannot exceed 100")
    private Integer weightage;
}
