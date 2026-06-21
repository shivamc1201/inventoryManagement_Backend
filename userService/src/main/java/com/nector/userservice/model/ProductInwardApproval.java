package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_inward_approvals")
@Data
public class ProductInwardApproval {

    public enum ProductType {
        RAW_PRODUCT, PROMOTIONAL_ITEM, SCRAP_ITEM, MACHINE_PART
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private Long productId;

    @Column
    private String productCode;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String requestPayload;

    @Column(nullable = false)
    private String approvalStatus = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedOn;

    private LocalDateTime reviewedOn;

    private String reviewedBy;

    @Column(length = 500)
    private String reviewComments;

    @PrePersist
    protected void onCreate() {
        requestedOn = LocalDateTime.now();
    }
}
