package com.nector.userservice.dto;

import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.enums.KPIMeasurementUnit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiMasterCreateRequest {

    @NotBlank(message = "KPI name is required")
    @Size(max = 255, message = "KPI name cannot exceed 255 characters")
    private String kpiName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "KPI category is required")
    private KPICategory kpiCategory;

    @NotNull(message = "Measurement unit is required")
    private KPIMeasurementUnit measurementUnit;

    @NotNull(message = "Default weightage is required")
    @Min(value = 0, message = "Default weightage cannot be negative")
    @Max(value = 100, message = "Default weightage cannot exceed 100")
    private Integer defaultWeightage;

    @NotNull(message = "Frequency is required")
    private KPIFrequency frequency;

    private Boolean isActive = true;
}
