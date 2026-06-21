package com.nector.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiAssignmentRequest {

    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Employee name cannot exceed 255 characters")
    private String employeeName;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Size(max = 100, message = "Designation cannot exceed 100 characters")
    private String designation;

    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    private String roleName;

    @NotNull(message = "KPI ID is required")
    private Long kpiId;

    @NotNull(message = "Target value is required")
    @Min(value = 0, message = "Target value must be at least 0")
    private BigDecimal targetValue;

    @NotNull(message = "Weightage is required")
    @Min(value = 0, message = "Weightage must be at least 0")
    @Max(value = 100, message = "Weightage cannot exceed 100")
    private Integer weightage;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public LocalDate getEndDate() {
        if (startDate == null) return null;
        return YearMonth.from(startDate).atEndOfMonth();
    }
}
