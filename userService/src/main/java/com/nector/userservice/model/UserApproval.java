package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_approvals")
@Data
public class UserApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedOn;
    
    private LocalDateTime reviewedOn;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private HrMaster reviewedBy;
    
    private String reviewComments;
    
    @PrePersist
    protected void onCreate() {
        requestedOn = LocalDateTime.now();
    }
}