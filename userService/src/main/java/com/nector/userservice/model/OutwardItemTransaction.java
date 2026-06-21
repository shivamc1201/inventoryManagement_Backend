package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outward_item_transactions", indexes = {
    @Index(name = "idx_outward_item_type", columnList = "itemType"),
    @Index(name = "idx_outward_transaction_type", columnList = "transactionType"),
    @Index(name = "idx_outward_material_code", columnList = "materialCode"),
    @Index(name = "idx_outward_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutwardItemTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, length = 50)
    private String materialCode;

    @Column(nullable = false, length = 200)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String comments;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal quotedSellingPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String referenceNumber;

    @Column(length = 200)
    private String issuedTo;

    public enum ItemType {
        SPARE_PARTS, PROMOTIONAL_ITEMS, SCRAP_MATERIAL
    }

    public enum TransactionType {
        OUTWARD_GIVING, RETURNED_PART
    }

    public enum Unit {
        KILOGRAMS, DOZENS, PIECES, LITRES
    }
}
