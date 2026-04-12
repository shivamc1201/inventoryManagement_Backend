package com.nector.userservice.interceptors.distributor.model;

import com.nector.userservice.enums.SalesRole;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "distributors")
@Data
public class Distributor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Column(name = "assigned_person", nullable = false, length = 100)
    private String assignedPerson;

    @Column(name = "salesperson_id")
    private Long salespersonId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "salesperson_role_type")
    private SalesRole salesPersonRoleType;
    
    @Column(name = "distributor_type", nullable = false)
    private String distributorType;
    
    @Column(name = "company_type", nullable = false)
    private String companyType;
    
    @Column(name = "contact_email", nullable = false, unique = true)
    private String contactEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "alternate_contact", length = 20)
    private String alternateContact;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;
    
    @Column(name = "address", length = 200)
    private String address;
    
    @Column(name = "aadhaar_number", unique = true, length = 12)
    private String aadhaarNumber;
    
    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DistributorStatus status;
    
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "account_name",  length = 100)
    private String accountName;

    @Column(name = "ifsc",  length = 100)
    private String ifsc;

    @Column(name = "distributor_code", nullable = false, unique = true, length = 15)
    private String distributorCode;

    @Column(name = "bank_guarantee_number", length = 50)
    private String bankGuaranteeNumber;

    @Column(name = "credit_amount", precision = 15, scale = 2)
    private java.math.BigDecimal creditAmount;

    @Column(name = "bg_expiry_date")
    private java.time.LocalDate bgExpiryDate;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "keyperson", length = 100)
    private String keyperson;

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
