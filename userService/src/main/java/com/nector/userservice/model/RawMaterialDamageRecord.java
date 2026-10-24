package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "raw_material_damage_records",
        indexes = @Index(name = "idx_rmdr_raw_material_id", columnList = "raw_material_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialDamageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "raw_material_name")
    private String rawMaterialName;

    @Column(name = "material_code", length = 100)
    private String materialCode;

    @Column(name = "quantity_damaged", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDamaged;

    @Column(name = "quantity_before", precision = 19, scale = 4)
    private BigDecimal quantityBefore;

    @Column(name = "quantity_after", precision = 19, scale = 4)
    private BigDecimal quantityAfter;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "damaged_by", length = 255)
    private String damagedBy;

    @CreationTimestamp
    @Column(name = "damaged_at", updatable = false)
    private Instant damagedAt;
}
