package com.nector.userservice.bom.entity;

import com.nector.userservice.bom.entity.AdditionalCost;
import com.nector.userservice.bom.entity.BomComponent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_of_materials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "finished_product_id", nullable = false)
    private Long finishedProductId;

    @Column(name = "finished_product_name", nullable = false)
    private String finishedProductName;

    @Column(name = "bom_name", nullable = false, unique = true)
    private String bomName;

    @Column(name = "output_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal outputQuantity;

    @Column(name = "output_unit", nullable = false)
    private String outputUnit;

    @Column(name = "cost_allocation_percent", precision = 5, scale = 2)
    private BigDecimal costAllocationPercent;

    @Column(name = "material_count")
    private Integer materialCount;

    @Column(name = "total_component_cost", precision = 19, scale = 2)
    private BigDecimal totalComponentCost;

    @Column(name = "total_additional_cost", precision = 19, scale = 2)
    private BigDecimal totalAdditionalCost;

    @Column(name = "effective_cost", precision = 19, scale = 2)
    private BigDecimal effectiveCost;

    @Column(name = "effective_rate_per_unit", precision = 19, scale = 2)
    private BigDecimal effectiveRatePerUnit;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BomComponent> components = new ArrayList<>();

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AdditionalCost> additionalCosts = new ArrayList<>();

    public void addComponent(BomComponent component) {
        components.add(component);
        component.setBom(this);
    }

    public void removeComponent(BomComponent component) {
        components.remove(component);
        component.setBom(null);
    }

    public void addAdditionalCost(AdditionalCost additionalCost) {
        additionalCosts.add(additionalCost);
        additionalCost.setBom(this);
    }

    public void removeAdditionalCost(AdditionalCost additionalCost) {
        additionalCosts.remove(additionalCost);
        additionalCost.setBom(null);
    }
}
