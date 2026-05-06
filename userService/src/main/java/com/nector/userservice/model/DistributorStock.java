package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "distributor_stock", 
       indexes = {
           @Index(name = "idx_dist_sku", columnList = "distributor_id, sku"),
           @Index(name = "idx_dist_item", columnList = "distributor_id, item_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id", nullable = false)
    private Long distributorId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_type", length = 50)
    private String unitType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
