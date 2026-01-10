package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String partNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    private String vendor;
    
    private LocalDate purchaseDate;
    
    private LocalDate warrantyExpiryDate;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition = Condition.NEW;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum Category {
        MACHINE, SPARE_PART, TOOL
    }
    
    public enum Condition {
        NEW, IN_USE, DAMAGED
    }
}