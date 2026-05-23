package com.nector.userservice.bom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bom_price_change_requests",
        indexes = {
                @Index(name = "idx_bpcr_raw_material_id", columnList = "raw_material_id"),
                @Index(name = "idx_bpcr_bom_id", columnList = "bom_id"),
                @Index(name = "idx_bpcr_status", columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomPriceChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "raw_material_name", nullable = false)
    private String rawMaterialName;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    @Column(name = "bom_name", nullable = false)
    private String bomName;

    @Column(name = "bom_component_id", nullable = false)
    private Long bomComponentId;

    @Column(name = "finished_product_id", nullable = false)
    private Long finishedProductId;

    @Column(name = "finished_product_name", nullable = false)
    private String finishedProductName;

    @Column(name = "old_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal oldRate;

    @Column(name = "new_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal newRate;

    @Column(name = "price_change_percent", precision = 10, scale = 2)
    private BigDecimal priceChangePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BomPriceChangeStatus status;

    /** Links back to the price history entry that triggered this request. */
    @Column(name = "price_history_id")
    private Long priceHistoryId;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private Instant requestedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Column(name = "notes", length = 1000)
    private String notes;
}
