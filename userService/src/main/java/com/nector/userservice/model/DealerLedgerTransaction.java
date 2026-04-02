package com.nector.userservice.model;

import com.nector.userservice.enums.LedgerTransactionCategory;
import com.nector.userservice.enums.LedgerTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_ledger_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerLedgerTransaction {

    @Id
    @Size(max = 30, message = "Transaction ID cannot exceed 30 characters")
    @Column(name = "id")
    private String id;

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column
    private LocalDate date;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column
    private String description;

    @Size(max = 100, message = "Reference cannot exceed 100 characters")
    @Column
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private LedgerTransactionType type;

    @Column(precision = 12, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LedgerTransactionCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
