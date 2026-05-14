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

    /**
     * Month (1-12) for which KPIs are being assigned.
     * If provided, startDate and endDate are auto-calculated as first and last day of that month.
     */
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    /**
     * Year for which KPIs are being assigned (e.g. 2026).
     */
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    /**
     * Optional override. If not provided, auto-set from month + year.
     */
    private LocalDate startDate;

    /**
     * Optional override. If not provided, auto-set from month + year (last day of month).
     */
    private LocalDate endDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    /**
     * Resolves the effective startDate.
     * If startDate is explicitly provided, use it; otherwise derive from month+year.
     */
    public LocalDate getEffectiveStartDate() {
        if (startDate != null) return startDate;
        if (month != null && year != null) return YearMonth.of(year, month).atDay(1);
        return null;
    }

    /**
     * Resolves the effective endDate.
     * If endDate is explicitly provided, use it; otherwise derive from month+year (last day).
     */
    public LocalDate getEffectiveEndDate() {
        if (endDate != null) return endDate;
        if (month != null && year != null) return YearMonth.of(year, month).atEndOfMonth();
        return null;
    }
}

