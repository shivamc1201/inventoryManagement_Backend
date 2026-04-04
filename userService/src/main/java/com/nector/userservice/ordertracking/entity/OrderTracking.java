package com.nector.userservice.ordertracking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_tracking", indexes = {
    @Index(name = "idx_ot_distributor", columnList = "distributor_id"),
    @Index(name = "idx_ot_order_number", columnList = "order_number"),
    @Index(name = "idx_ot_order_date", columnList = "order_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "distributor_id", nullable = false)
    private Long distributorId;

    @Column(name = "distributor_name", nullable = false)
    private String distributorName;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepSequence ASC")
    @Builder.Default
    private List<OrderTrackingStep> steps = new ArrayList<>();
}
