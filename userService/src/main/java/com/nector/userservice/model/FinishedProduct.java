package com.nector.userservice.model;

import com.nector.userservice.enums.ProductStatus;
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
    
    @Column
    private String name;
    
    private String description;
    
    @Column(unique = true)
    private String sku;
    
    @Enumerated(EnumType.STRING)
    @Column
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
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column
    private Integer quantity;
    
    @Column(name = "minimum_threshold")
    private Integer minimumThreshold;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;
    
    @Column(length = 50)
    private String hsn;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.MASTER;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(precision = 10, scale = 2)
    private BigDecimal gst;

    @Column(precision = 10, scale = 2)
    private BigDecimal grossAmount;
}
