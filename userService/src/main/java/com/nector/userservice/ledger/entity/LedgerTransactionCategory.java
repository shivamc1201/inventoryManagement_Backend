package com.nector.userservice.ledger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ledger Transaction Category Entity
 * Defines categories for ledger transactions
 */
@Entity
@Table(name = "ledger_transaction_category")
@Data
@EqualsAndHashCode(of = "id")
public class LedgerTransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public LedgerTransactionCategory() {}

    public LedgerTransactionCategory(String categoryCode, String categoryName, String description) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
    }
}