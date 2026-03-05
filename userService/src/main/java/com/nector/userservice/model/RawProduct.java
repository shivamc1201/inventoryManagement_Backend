package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_products", indexes = {
    @Index(name = "idx_raw_product_material_code", columnList = "materialCode", unique = true),
    @Index(name = "idx_raw_product_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String materialCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;
    
    @Column(length = 50)
    private String unitType;
    
    @Column(length = 50)
    private String productSize;
    
    @Column(length = 100)
    private String unitName;
    
    @Column(unique = true, length = 50)
    private String unitCode;
    
    @Column(length = 500)
    private String unitDescription;
    
    @Column(length = 20)
    private String unitStatus;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(nullable = false)
    private Integer minimumThreshold = 0;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum Unit {
        KG, LITER, PIECE
    }
}