package com.nector.userservice.ordertracking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_tracking_steps", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "step_sequence"}),
       indexes = {
           @Index(name = "idx_ots_order", columnList = "order_id"),
           @Index(name = "idx_ots_distributor_id", columnList = "distributor_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderTracking order;

    @Column(name = "step_sequence", nullable = false)
    private Integer stepSequence;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StepStatus status;

    @Column(name = "delivery_by")
    private String deliveryBy;

    @Column(name = "step_date")
    private LocalDate stepDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // Assigned person (denormalized for one-query reads)
    @Column(name = "assigned_person_id")
    private Long assignedPersonId;

    @Column(name = "assigned_person_name")
    private String assignedPersonName;

    @Column(name = "assigned_person_role")
    private String assignedPersonRole;

    @Column(name = "assigned_person_phone")
    private String assignedPersonPhone;

    @Column(name = "assigned_person_email")
    private String assignedPersonEmail;

    // Document
    @Column(name = "has_download")
    private boolean hasDownload;

    @Column(name = "download_label")
    private String downloadLabel;

    @Column(name = "document_path")
    private String documentPath;

    // Action (step 11 only)
    @Column(name = "has_action")
    private boolean hasAction;

    @Column(name = "action_response", length = 10)
    private String actionResponse;     // "yes" | "no" | null

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Direct distributor reference for performance and filtering
    @Column(name = "distributor_id")
    private Long distributorId;
}
