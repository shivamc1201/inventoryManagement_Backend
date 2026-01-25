package com.nector.userservice.interceptors.distributor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "distributors")
@Data
public class Distributor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String assignedPerson;
    
    @Column(nullable = false)
    private String distributorType;
    
    @Column(nullable = false)
    private String companyType;
    
    @Column(nullable = false, unique = true)
    private String contactEmail;
    
    @Column(nullable = false, length = 15)
    private String phoneNumber;
    
    @Column(length = 15)
    private String alternateContact;
    
    @Column(nullable = false, length = 200)
    private String address;
    
    @Column(nullable = false, unique = true, length = 12)
    private String aadhaarNumber;
    
    @Column(nullable = false, unique = true, length = 10)
    private String panNumber;
    
    @Column(nullable = false, unique = true, length = 15)
    private String gstNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributorStatus status;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    @Column
    private LocalDateTime updatedOn;
    
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