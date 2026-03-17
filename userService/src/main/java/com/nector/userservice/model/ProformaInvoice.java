package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proforma_invoices")
@Data
public class ProformaInvoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pi_number", nullable = false)
    private String piNumber;
    
    @Column(name = "cart_id")
    private Long cartId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "distributor_id")
    private Long distributorId;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "distributor_name")
    private String distributorName;
    
    public enum PaymentStatus {
        PENDING, PAID, REJECTED
    }
}
