package com.nector.userservice.interceptors.distributor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_confirmations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmation {

    public enum ApprovalStatus {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        NOT_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long distributorId;

    @Column(nullable = false)
    private String gdnNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderConfirmationRequest.ConfirmationStatus status;

    private Integer overallRating;
    private String feedback;
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'NOT_REQUIRED'")
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_REQUIRED;

    @Column(length = 1000)
    private String adminComment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "orderConfirmation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemConfirmationEntity> itemConfirmations = new ArrayList<>();
}