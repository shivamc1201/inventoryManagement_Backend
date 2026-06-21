package com.nector.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiBulkAssignmentRequest {

    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Employee name cannot exceed 255 characters")
    private String employeeName;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @Size(max = 100, message = "Designation cannot exceed 100 characters")
    private String designation;

    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    private String roleName;

    @NotEmpty(message = "At least one KPI assignment is required")
    @Valid
    private List<KpiBulkAssignmentItem> assignments;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public LocalDate getEffectiveStartDate() {
        return YearMonth.of(year, month).atDay(1);
    }

    public LocalDate getEffectiveEndDate() {
        return YearMonth.of(year, month).atEndOfMonth();
    }
}

