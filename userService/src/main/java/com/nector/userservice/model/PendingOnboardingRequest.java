package com.nector.userservice.model;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.enums.UserOnboardingType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_onboarding_requests")
@Data
public class PendingOnboardingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_onboarding_type", nullable = false)
    private UserOnboardingType userOnboardingType;

    // Common fields
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String contactNo;
    private String alternateContactNo;
    private String bloodGroup;

    @Column(columnDefinition = "TEXT")
    private String completeAddress;

    private String city;
    private String country;
    private String zip;
    private LocalDate dateOfBirth;
    private String gender;

    @Column(length = 50)
    private String employeeRollNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // USER-specific
    private String roleType;

    // SALES-specific
    @Enumerated(EnumType.STRING)
    private SalesRole salesRole;
    private String zone;
    private String region;

    // Approval metadata
    @Column(nullable = false)
    private String approvalStatus = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedOn;

    private LocalDateTime reviewedOn;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    private String reviewComments;

    @PrePersist
    protected void onCreate() {
        requestedOn = LocalDateTime.now();
    }
}
