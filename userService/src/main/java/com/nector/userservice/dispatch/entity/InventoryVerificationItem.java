package com.nector.userservice.dispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "inventory_verification_items")
public class InventoryVerificationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;
    private String itemName;
    private String itemSku;
    private Integer orderedQuantity;
    private Integer availableQuantity;
    
    @Column(name = "stock_status")
    private String stockStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id")
    private InventoryVerification inventoryVerification;
}