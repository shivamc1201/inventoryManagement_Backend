package com.nector.userservice.interceptors.distributor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_confirmations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemConfirmationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_confirmation_id", nullable = false)
    private OrderConfirmation orderConfirmation;
    
    @Column(nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private Integer dispatchedQuantity;
    
    @Column(nullable = false)
    private Integer receivedQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderConfirmationRequest.ItemCondition condition;
    
    private String itemRemarks;
}