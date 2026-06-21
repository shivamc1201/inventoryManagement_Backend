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

    @Column(name = "delivery_by")
    private String deliveryBy;

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

    
    @Column(name = "total_weight", precision = 12, scale = 3)
    private BigDecimal totalWeight;

    public BigDecimal calculateTotalWeight() {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cartItems.stream()
            .filter(item -> item.getItem() != null)
            .map(item -> calculateItemWeight(item))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateItemWeight(CartItem cartItem) {
        FinishedProduct product = cartItem.getItem();
        BigDecimal weight = product.getWeight();
        int quantity = cartItem.getQuantity();
        
        if (product.getUnit() == null || weight == null || weight.equals(BigDecimal.ZERO)) {
            // Fallback: extract weight from product name
            return extractWeightFromProductName(product.getName(), quantity);
        }
        
        switch (product.getUnit()) {
            case KG:
                // Direct weight calculation for KG units
                return weight.multiply(BigDecimal.valueOf(quantity));
                
            case LITER:
                // For liters, assume weight is already in kg per liter
                // Could add density conversion if needed in future
                return weight.multiply(BigDecimal.valueOf(quantity));
                
            case DOZEN:
                // For dozen, calculate weight per dozen and convert to pieces
                BigDecimal weightPerPiece = weight.divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP);
                return weightPerPiece.multiply(BigDecimal.valueOf(quantity));
                
            case PIECES:
                // Direct weight calculation for pieces
                return weight.multiply(BigDecimal.valueOf(quantity));
                
            default:
                // Fallback to simple calculation
                return weight.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private BigDecimal extractWeightFromProductName(String productName, int quantity) {
        try {
            // Extract weight from product name (e.g., "50 Kg" from "MANKA MAHISHI 50 Kg")
            String[] words = productName.split(" ");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].matches("\\d+(\\.\\d+)?") &&
                        (words[i + 1].equalsIgnoreCase("Kg") || words[i + 1].equalsIgnoreCase("Kg"))) {

                    BigDecimal weightPerUnit = new BigDecimal(words[i]);
                    return weightPerUnit.multiply(BigDecimal.valueOf(quantity));
                }
            }
        } catch (Exception e) {
            // Log error if needed, but return 0
        }

        return BigDecimal.ZERO;
    }

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
