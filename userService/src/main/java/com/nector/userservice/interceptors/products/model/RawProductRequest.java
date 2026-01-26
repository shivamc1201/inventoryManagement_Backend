package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.RawProduct;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RawProductRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Material code is required")
    @Size(max = 100, message = "Material code must not exceed 100 characters")
    private String materialCode;
    
    @NotNull(message = "Unit is required")
    private RawProduct.Unit unit;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    @NotNull(message = "Minimum threshold is required")
    @Min(value = 0, message = "Minimum threshold must be non-negative")
    private Integer minimumThreshold;
}