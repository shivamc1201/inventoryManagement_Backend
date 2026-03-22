package com.nector.userservice.interceptors.distributor.model;

import com.nector.userservice.enums.SalesRole;
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
    private String username;

    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, length = 100)
    private String assignedPerson;

    @Column(name = "salesperson_id")
    private Long salespersonId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "salesperson_role_type")
    private SalesRole salesPersonRoleType;
    
    @Column(nullable = false)
    private String distributorType;
    
    @Column(nullable = false)
    private String companyType;
    
    @Column(nullable = false, unique = true)
    private String contactEmail;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 20)
    private String alternateContact;

    @Column(length = 20)
    private String gstNumber;
    
    @Column(nullable = false, length = 200)
    private String address;
    
    @Column(nullable = false, unique = true, length = 12)
    private String aadhaarNumber;
    
    @Column(nullable = false, unique = true, length = 10)
    private String panNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributorStatus status;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    @Column
    private LocalDateTime updatedOn;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(nullable = false, length = 100)
    private String ifsc;


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
