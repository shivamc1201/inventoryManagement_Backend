package com.nector.userservice.model;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = "roles")
@ToString(exclude = "roles")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    @Column(length = 15)
    private String contactNo;
    
    @Column(length = 15)
    private String alternateContactNo;
    
    @Column(length = 10)
    private String bloodGroup;
    
    @Column(length = 200)
    private String completeAddress;
    
    private LocalDate dateOfBirth;
    
    @Column(length = 10)
    private String gender;

    @Column(length = 50)
    private String otp;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 20)
    @Size(max = 20, message = "ZIP code cannot exceed 20 characters")
    private String zip;
    
    private LocalDateTime lastLoginTime;
    
    @Column(nullable = false)
    private boolean isLoggedIn = false;
    
    @Column(nullable = false)
    private LocalDateTime passwordSetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleType;
    
    // Many-to-many relationship with roles for RBAC
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
        passwordSetDate = LocalDateTime.now();
    }
}