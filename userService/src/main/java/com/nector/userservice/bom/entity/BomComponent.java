package com.nector.userservice.bom.entity;

import com.nector.userservice.bom.entity.BillOfMaterial;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bom_components")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial bom;

    @Column(name = "raw_material_id", nullable = false)
    private Long rawMaterialId;

    @Column(name = "raw_material_name", nullable = false)
    private String rawMaterialName;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal rate;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "contribution_percent", precision = 5, scale = 2)
    private BigDecimal contributionPercent;
}
