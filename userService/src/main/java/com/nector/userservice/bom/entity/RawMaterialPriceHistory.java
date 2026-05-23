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
@Table(name = "raw_material_price_history",
        indexes = @Index(name = "idx_rmph_raw_material_id", columnList = "raw_material_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "raw_material_name", nullable = false)
    private String rawMaterialName;

    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "old_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal newPrice;

    /** Positive = price hike, negative = price drop, expressed as percentage. */
    @Column(name = "price_change_percent", precision = 10, scale = 2)
    private BigDecimal priceChangePercent;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private Instant changedAt;
}
