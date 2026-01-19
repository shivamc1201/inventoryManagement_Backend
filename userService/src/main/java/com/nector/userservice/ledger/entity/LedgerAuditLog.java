package com.nector.userservice.ledger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Ledger Audit Log Entity
 * Tracks all changes and actions performed on ledger accounts and transactions
 */
@Entity
@Table(name = "ledger_audit_log")
@Data
@EqualsAndHashCode(of = "id")
public class LedgerAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount ledgerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private LedgerTransaction transaction;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "old_values")
    @JdbcTypeCode(SqlTypes.JSON)
    private String oldValues;

    @Column(name = "new_values")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValues;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Constructors
    public LedgerAuditLog() {}

    public LedgerAuditLog(LedgerAccount ledgerAccount, String actionType, String userId) {
        this.ledgerAccount = ledgerAccount;
        this.actionType = actionType;
        this.userId = userId;
    }

    public LedgerAuditLog(LedgerAccount ledgerAccount, LedgerTransaction transaction, 
                         String actionType, String userId) {
        this.ledgerAccount = ledgerAccount;
        this.transaction = transaction;
        this.actionType = actionType;
        this.userId = userId;
    }
}