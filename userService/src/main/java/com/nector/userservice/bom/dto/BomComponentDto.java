package com.nector.userservice.bom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomComponentDto {

    private Long id;

    @NotNull(message = "Raw material ID is required")
    private Long rawMaterialId;

    @NotBlank(message = "Raw material name is required")
    private String rawMaterialName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    private BigDecimal rate;

    private BigDecimal amount;

    private BigDecimal contributionPercent;
}
