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
@Table(name = "raw_material_inventory_lots",
        indexes = @Index(name = "idx_rmil_raw_material_id", columnList = "raw_material_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialInventoryLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "quantity_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOriginal;

    @Column(name = "quantity_remaining", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityRemaining;

    @Column(name = "price_per_unit", nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerUnit;

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private Instant receivedAt;
}
