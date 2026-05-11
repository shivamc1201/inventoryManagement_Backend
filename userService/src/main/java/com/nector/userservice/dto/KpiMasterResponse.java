package com.nector.userservice.dto;

import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.enums.KPIMeasurementUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiMasterResponse {

    private Long id;
    private String kpiCode;
    private String kpiName;
    private String description;
    private KPICategory kpiCategory;
    private KPIMeasurementUnit measurementUnit;
    private Integer defaultWeightage;
    private KPIFrequency frequency;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
