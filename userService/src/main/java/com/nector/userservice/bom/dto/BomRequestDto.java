package com.nector.userservice.bom.dto;

import com.nector.userservice.bom.dto.AdditionalCostDto;
import com.nector.userservice.bom.dto.BomComponentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomRequestDto {

    @NotNull(message = "Finished product ID is required")
    private Long finishedProductId;

    @NotBlank(message = "Finished product name is required")
    private String finishedProductName;

    @NotBlank(message = "BOM name is required")
    private String bomName;

    @NotNull(message = "Output quantity is required")
    @Positive(message = "Output quantity must be positive")
    @DecimalMin(value = "0.01", message = "Output quantity must be at least 0.01")
    private BigDecimal outputQuantity;

    @NotBlank(message = "Output unit is required")
    private String outputUnit;

    @DecimalMin(value = "0.01", message = "Cost allocation percentage must be at least 0.01")
    @DecimalMax(value = "100.00", message = "Cost allocation percentage cannot exceed 100")
    private BigDecimal costAllocationPercent;

    @NotEmpty(message = "Components list cannot be empty")
    @Valid
    private List<BomComponentDto> components;

    @Valid
    private List<AdditionalCostDto> additionalCosts;
}
