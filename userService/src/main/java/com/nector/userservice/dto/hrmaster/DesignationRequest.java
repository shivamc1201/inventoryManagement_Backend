package com.nector.userservice.dto.hrmaster;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DesignationRequest {
    @NotBlank(message = "Designation name is required")
    @Size(max = 100, message = "Designation name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Department ID is required")
    private Long departmentId;
}