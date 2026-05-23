package com.nector.userservice.model;

import com.nector.userservice.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_parts", indexes = {
    @Index(name = "idx_machine_part_part_number", columnList = "partNumber", unique = true),
    @Index(name = "idx_machine_part_active", columnList = "active"),
    @Index(name = "idx_machine_part_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachinePart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String name;
    
    @Column(unique = true)
    private String partNumber;
    
    @Enumerated(EnumType.STRING)
    @Column
    private Category category;
    
    private String vendor;
    
    private LocalDate purchaseDate;
    
    private LocalDate warrantyExpiryDate;
    
    @Column
    private Integer quantity = 0;

    @Column(length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column
    private Condition condition = Condition.New;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String hsn;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.MASTER;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(precision = 10, scale = 2)
    private BigDecimal gst;

    @Column(precision = 10, scale = 2)
    private BigDecimal grossAmount;

    public enum Category {
        Machine, MACHINE, Electrical, ELECTRICAL, Hydraulic, HYDRAULIC, Pneumatic, PNEUMATIC, Tool, TOOL, SparePart, SPARE_PART, Other, OTHER
    }
    
    public enum Condition {
        New, NEW, Used, USED, InUse, IN_USE, Refurbished, REFURBISHED, Damaged, DAMAGED
    }
}