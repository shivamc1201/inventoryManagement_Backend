package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scrap_outward_approvals")
@Data
public class ScrapOutwardApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long outwardTransactionId;

    @Column(nullable = false, length = 50)
    private String materialCode;

    @Column(nullable = false, length = 200)
    private String materialName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal quotedSellingPrice;

    @Column(length = 200)
    private String issuedTo;

    @Column(nullable = false, length = 20)
    private String approvalStatus = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedOn;

    private LocalDateTime reviewedOn;

    @Column(length = 100)
    private String reviewedBy;

    @Column(length = 500)
    private String reviewComments;

    @PrePersist
    protected void onCreate() {
        requestedOn = LocalDateTime.now();
    }
}
