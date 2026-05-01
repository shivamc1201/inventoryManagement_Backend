package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.model.MachinePart;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateMachinePart {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

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

    private String hsn;

    private BigDecimal taxRate;

    // Explicit getters/setters for hsn and taxRate to ensure compilation
    public String getHsn() {
        return hsn;
    }

    public void setHsn(String hsn) {
        this.hsn = hsn;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}
