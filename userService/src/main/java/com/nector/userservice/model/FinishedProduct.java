package com.nector.userservice.model;

import com.nector.userservice.enums.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finished_products", indexes = {
    @Index(name = "idx_finished_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_finished_product_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishedProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;
    
    @Column(precision = 10, scale = 3)
    private BigDecimal weight;
    
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
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, name = "minimum_threshold")
    private Integer minimumThreshold;
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
