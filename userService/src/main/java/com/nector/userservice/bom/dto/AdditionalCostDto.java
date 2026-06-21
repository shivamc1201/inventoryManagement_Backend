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
public class AdditionalCostDto {

    private Long id;

    @NotBlank(message = "Cost type is required")
    private String type;

    @DecimalMin(value = "0.01", message = "Percentage must be greater than 0")
    private BigDecimal percentage;

    private BigDecimal amount;
}
