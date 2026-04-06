package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.Unit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinishedProductRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;
    
    @NotNull(message = "Unit is required")
    private Unit unit;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be non-negative")
    @Digits(integer = 7, fraction = 3, message = "Weight must have at most 7 integer digits and 3 decimal places")
    private BigDecimal weight;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    @NotNull(message = "Minimum threshold is required")
    @Min(value = 0, message = "Minimum threshold must be non-negative")
    private Integer minimumThreshold;
    
    private Boolean active;
}
