package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_invoices")
@Data
public class DealerInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "dealer_order_id", unique = true)
    private Long dealerOrderId;

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "dealer_name")
    private String dealerName;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "distributor_name")
    private String distributorName;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "grand_total")
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status")
    private InvoiceStatus invoiceStatus = InvoiceStatus.GENERATED;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum InvoiceStatus {
        GENERATED, PAID, CANCELLED
    }
}
