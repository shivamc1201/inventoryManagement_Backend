package com.nector.userservice.interceptors.salesMapping.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "salesperson_distributor_mapping")
@Data
public class SalespersonDistributorMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long salespersonId;
    
    @Column(nullable = false)
    private Long distributorId;
    
    @Column(nullable = false)
    private Long companyId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MappingStatus status;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    @Column
    private LocalDateTime updatedOn;
    
    @Column(length = 100)
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
        updatedOn = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedOn = LocalDateTime.now();
    }
}