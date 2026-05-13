package com.nector.userservice.model;

import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.enums.KPIMeasurementUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50, message = "KPI code cannot exceed 50 characters")
    @Column(name = "kpi_code", unique = true, length = 50)
    private String kpiCode;

    @Size(max = 255, message = "KPI name cannot exceed 255 characters")
    @Column(name = "kpi_name", length = 255)
    private String kpiName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "kpi_category", length = 20)
    private KPICategory kpiCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", length = 20)
    private KPIMeasurementUnit measurementUnit;

    @Min(value = 0, message = "Default weightage must be at least 0")
    @Max(value = 100, message = "Default weightage cannot exceed 100")
    @Column(name = "default_weightage")
    private Integer defaultWeightage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private KPIFrequency frequency;

    @Column(name = "is_active")
    private Boolean isActive = true;


    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (kpiCode == null || kpiCode.isBlank()) {
            kpiCode = generateKpiCode();
        }
    }

    private String generateKpiCode() {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "KPI-" + timestamp + "-" + System.currentTimeMillis() % 1000;
    }
}
