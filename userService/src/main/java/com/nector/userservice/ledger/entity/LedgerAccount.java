package com.nector.userservice.ledger.entity;

import com.nector.userservice.ledger.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ledger Account Entity - One per Distributor
 * Represents the main ledger account for each distributor
 */
@Entity
@Table(name = "ledger_account")
@Data
@EqualsAndHashCode(of = "id")
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "distributor_id", nullable = false)
    private Long distributorId;

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType = "DISTRIBUTOR";

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    // Constructors
    public LedgerAccount() {}

    public LedgerAccount(Long companyId, Long distributorId, String accountName, String createdBy) {
        this.companyId = companyId;
        this.distributorId = distributorId;
        this.accountName = accountName;
        this.createdBy = createdBy;
        this.accountNumber = generateAccountNumber(companyId, distributorId);
    }

    // Business methods
    private String generateAccountNumber(Long companyId, Long distributorId) {
        return String.format("LA%06d%06d", companyId, distributorId);
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    public boolean canDebit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal newBalance = currentBalance.subtract(amount);
        return newBalance.add(creditLimit).compareTo(BigDecimal.ZERO) >= 0;
    }
}