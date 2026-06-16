package com.nector.userservice.transaction.entity;

import com.nector.userservice.transaction.enums.FundLocation;
import com.nector.userservice.transaction.enums.PaymentMode;
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
@Table(name = "transaction_fund")
@Data
@EqualsAndHashCode(of = "id")
public class TransactionFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fund_no", nullable = false, unique = true, length = 20)
    private String fundNo;

    @Column(name = "fund_date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 8)
    private PaymentMode paymentMode;

    @Column(name = "transaction_id", length = 60)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false, length = 8)
    private FundLocation location;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}
