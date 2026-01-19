package com.nector.userservice.ledger.entity;

import com.nector.userservice.ledger.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ledger Transaction Entity - Append-only transactions
 * Represents individual financial transactions in the ledger
 */
@Entity
@Table(name = "ledger_transaction")
@Data
@EqualsAndHashCode(of = "id")
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount ledgerAccount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Column(name = "reference_number", nullable = false, length = 100)
    private String referenceNumber;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "debit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "running_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal runningBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private LedgerTransactionCategory category;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "is_reversal", nullable = false)
    private Boolean isReversal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_transaction_id")
    private LedgerTransaction reversedTransaction;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    // Constructors
    public LedgerTransaction() {}

    public LedgerTransaction(LedgerAccount ledgerAccount, LocalDate transactionDate, 
                           TransactionType transactionType, String referenceNumber, 
                           String description, String createdBy) {
        this.ledgerAccount = ledgerAccount;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.referenceNumber = referenceNumber;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Business methods
    public BigDecimal getTransactionAmount() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0 ? debitAmount : creditAmount;
    }

    public boolean isDebitTransaction() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCreditTransaction() {
        return creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public void setAsDebit(BigDecimal amount) {
        this.debitAmount = amount;
        this.creditAmount = BigDecimal.ZERO;
    }

    public void setAsCredit(BigDecimal amount) {
        this.creditAmount = amount;
        this.debitAmount = BigDecimal.ZERO;
    }
}