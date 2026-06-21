package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_masters")
@Data
public class HrMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    
    private LocalDateTime lastLoginTime;
    
    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}