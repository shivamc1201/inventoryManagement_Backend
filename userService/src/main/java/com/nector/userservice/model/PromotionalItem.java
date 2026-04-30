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
@Table(name = "promotional_items", indexes = {
    @Index(name = "idx_promotional_item_code", columnList = "itemCode", unique = true),
    @Index(name = "idx_promotional_item_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionalItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String name;
    
    @Column(unique = true)
    private String itemCode;
    
    @Enumerated(EnumType.STRING)
    @Column
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
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column
    private Integer quantity;
    
    @Column
    private Integer minimumThreshold;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String vendorId;

    @Column(length = 255)
    private String vendorName;

    @Column(length = 255)
    private String transportName;

    @Column(length = 100)
    private String driverName;

    @Column(length = 20)
    private String driverMobile;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.MASTER;
}
