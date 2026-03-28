package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_distributor_status", columnList = "distributor_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Column(name = "salesperson_name")
    private String salespersonName;

    @Column(name = "distributor_name")
    private String distributorName;

    @Column(name = "total_cart_amount", precision = 12, scale = 2)
    private BigDecimal totalCartAmount;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "dismiss_reason")
    private String dismissReason;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CartStatus {
        ACTIVE,
        CHECKED_OUT,
        APPROVED,
        PAYMENT_APPROVED,
        GDN_GENERATED,
        PLACED,
        DISMISSED,
        GDN_REJECTED
    }
}
