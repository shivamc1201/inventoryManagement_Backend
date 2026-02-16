package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "distributor_ledger")
@Data
public class DistributorLedger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "distributor_id")
    private Long distributorId;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "transaction_type")
    private String transactionType; // CREDIT, DEBIT
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}