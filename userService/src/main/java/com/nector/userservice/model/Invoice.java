package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "order_confirmation_id")
    private Long orderConfirmationId;
    
    @Column(name = "distributor_id")
    private Long distributorId;
    
    @Column(name = "distributor_name")
    private String distributorName;
    
    @Column(name = "gdn_number")
    private String gdnNumber;
    
    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate = LocalDateTime.now();
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @Column(name = "tax_amount")
    private BigDecimal taxAmount;
    
    @Column(name = "grand_total")
    private BigDecimal grandTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status")
    private InvoiceStatus invoiceStatus = InvoiceStatus.GENERATED;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "pdf_url")
    private String pdfUrl;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "payment_terms")
    private String paymentTerms = "Due on Receipt";
    
    public enum InvoiceStatus {
        GENERATED, PAID, CANCELLED
    }
}
