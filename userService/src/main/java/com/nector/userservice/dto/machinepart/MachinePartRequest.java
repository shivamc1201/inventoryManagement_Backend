package com.nector.userservice.dto.machinepart;

import com.nector.userservice.model.MachinePart;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MachinePartRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Part number is required")
    @Size(max = 100, message = "Part number must not exceed 100 characters")
    private String partNumber;
    
    @NotNull(message = "Category is required")
    private MachinePart.Category category;
    
    @Size(max = 255, message = "Vendor must not exceed 255 characters")
    private String vendor;
    
    private LocalDate purchaseDate;
    
    private LocalDate warrantyExpiryDate;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    @NotNull(message = "Condition is required")
    private MachinePart.Condition condition;
}