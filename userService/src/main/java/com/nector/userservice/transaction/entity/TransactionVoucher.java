package com.nector.userservice.transaction.entity;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.enums.VoucherStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction_voucher")
@Data
@EqualsAndHashCode(of = "id")
public class TransactionVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_no", nullable = false, unique = true, length = 20)
    private String voucherNo;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", nullable = false, length = 16)
    private LedgerType voucherType;

    @Column(name = "ledger_id")
    private Long ledgerId;

    @Column(name = "ledger_name", nullable = false, length = 120)
    private String ledgerName;

    @Column(name = "party_name", length = 150)
    private String partyName;

    @Column(name = "mobile_no", length = 20)
    private String mobileNo;

    @Column(name = "invoice_ref", length = 60)
    private String invoiceRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 8)
    private PaymentMode paymentMode;

    @Column(name = "transaction_id", length = 60)
    private String transactionId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "less_adjustment", nullable = false, precision = 15, scale = 2)
    private BigDecimal lessAdjustment = BigDecimal.ZERO;

    @Column(name = "narration", nullable = false, columnDefinition = "TEXT")
    private String narration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private VoucherStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
