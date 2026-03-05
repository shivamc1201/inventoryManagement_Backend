package com.nector.userservice.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitRequestDTO {
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotBlank(message = "Unit type is required")
    @Size(max = 50, message = "Unit type must not exceed 50 characters")
    private String unitType;
    
    @Size(max = 50, message = "Product size must not exceed 50 characters")
    private String productSize;
    
    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String unitName;
    
    @NotBlank(message = "Unit code is required")
    @Size(max = 50, message = "Unit code must not exceed 50 characters")
    private String unitCode;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Status is required")
    private String status;
}
