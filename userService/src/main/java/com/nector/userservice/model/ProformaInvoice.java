package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proforma_invoices")
@Data
public class ProformaInvoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String piNumber;
    
    @Column(nullable = false)
    private Long cartId;
    
    @Column(nullable = false)
    private Long userId;
    
    @CreationTimestamp
    private LocalDateTime piDate;
    
    // Seller Details
    private String sellerCompanyName;
    private String sellerAddress;
    private String sellerGstin;
    private String sellerCin;
    private String sellerContactNumber;
    private String sellerEmail;
    
    // Buyer Details
    private String buyerName;
    private String buyerBillingAddress;
    private String buyerShippingAddress;
    private String buyerState;
    private String buyerStateCode;
    private String buyerGstin;
    private String buyerContactNumber;
    
    // Invoice Metadata
    private String modeOfPayment = "100% Advance";
    private String buyerOrderNumber;
    private String deliveryNote;
    private String dispatchDocumentNumber;
    private String termsOfDelivery;
    
    // Totals
    private BigDecimal subtotal;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal grandTotal;
    private String amountInWords;
    
    @OneToMany(mappedBy = "proformaInvoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProformaInvoiceItem> items;
}