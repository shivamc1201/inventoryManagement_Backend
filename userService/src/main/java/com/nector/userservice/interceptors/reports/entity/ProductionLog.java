package com.nector.userservice.interceptors.reports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_logs", indexes = {
    @Index(name = "idx_pl_product_id", columnList = "finished_product_id"),
    @Index(name = "idx_pl_production_date", columnList = "production_date"),
    @Index(name = "idx_pl_batch_number", columnList = "batch_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_number", nullable = false, unique = true, length = 50)
    private String productionNumber;

    @Column(name = "bom_id")
    private Long bomId;

    @Column(name = "finished_product_id", nullable = false)
    private Long finishedProductId;

    @Column(name = "finished_product_name", length = 200)
    private String finishedProductName;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "quantity_produced", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityProduced;

    @Column(name = "output_unit", length = 20)
    private String outputUnit;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "shift", length = 20)
    private String shift;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Column(name = "total_raw_material_cost", precision = 19, scale = 2)
    private BigDecimal totalRawMaterialCost;

    @Column(name = "total_additional_cost", precision = 19, scale = 2)
    private BigDecimal totalAdditionalCost;

    @Column(name = "total_production_cost", precision = 19, scale = 2)
    private BigDecimal totalProductionCost;

    @Column(name = "cost_per_unit", precision = 19, scale = 4)
    private BigDecimal costPerUnit;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "IN_PROGRESS";

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "productionLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionLogComponent> components = new ArrayList<>();
}
