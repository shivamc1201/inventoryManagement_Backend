package com.nector.userservice.dispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "inventory_verifications")
public class InventoryVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true)
    private Long orderId;

    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    private String status;

    @Column(name = "can_proceed")
    private boolean canProceed;

    @OneToMany(mappedBy = "inventoryVerification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryVerificationItem> items;
}