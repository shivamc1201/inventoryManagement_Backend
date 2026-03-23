package com.nector.userservice.bom.entity;

import com.nector.userservice.bom.entity.BillOfMaterial;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "additional_costs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial bom;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
}
