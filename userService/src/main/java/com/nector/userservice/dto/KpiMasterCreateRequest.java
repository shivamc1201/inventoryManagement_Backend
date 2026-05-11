package com.nector.userservice.dto;

import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.enums.KPIMeasurementUnit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiMasterCreateRequest {

    @Size(max = 255, message = "KPI name cannot exceed 255 characters")
    private String kpiName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private KPICategory kpiCategory;

    private KPIMeasurementUnit measurementUnit;

    @Min(value = 0, message = "Default weightage must be at least 0")
    @Max(value = 100, message = "Default weightage cannot exceed 100")
    private Integer defaultWeightage;

    private KPIFrequency frequency;

    private Boolean isActive = true;
}
