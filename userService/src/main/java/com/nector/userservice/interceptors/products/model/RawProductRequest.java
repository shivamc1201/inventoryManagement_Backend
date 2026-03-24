package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.RawProduct;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

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

    @Size(max = 50, message = "Vendor ID must not exceed 50 characters")
    private String vendorId;

    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    private String vendorName;

    @Size(max = 255, message = "Transport name must not exceed 255 characters")
    private String transportName;

    @Size(max = 100, message = "Driver name must not exceed 100 characters")
    private String driverName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Driver mobile must be a valid 10-digit number")
    private String driverMobile;
}