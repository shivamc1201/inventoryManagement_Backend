package com.nector.userservice.transaction.entity;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "transaction_ledger")
@Data
@EqualsAndHashCode(of = "id")
public class TransactionLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_name", nullable = false, unique = true, length = 120)
    private String ledgerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 16)
    private LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "under_group", nullable = false, length = 24)
    private UnderGroup underGroup;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
