package com.nector.userservice.interceptors.reports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "production_log_components", indexes = {
    @Index(name = "idx_plc_production_log_id", columnList = "production_log_id"),
    @Index(name = "idx_plc_raw_material_id", columnList = "raw_material_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_log_id", nullable = false)
    private ProductionLog productionLog;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "raw_material_name", length = 200)
    private String rawMaterialName;

    @Column(name = "quantity_planned", precision = 19, scale = 4)
    private BigDecimal quantityPlanned;

    @Column(name = "quantity_actual", precision = 19, scale = 4)
    private BigDecimal quantityActual;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "rate", precision = 19, scale = 2)
    private BigDecimal rate;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "variance_qty", precision = 19, scale = 4)
    private BigDecimal varianceQty;
}
