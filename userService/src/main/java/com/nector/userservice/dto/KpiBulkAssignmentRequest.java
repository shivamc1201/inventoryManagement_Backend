package com.nector.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}
