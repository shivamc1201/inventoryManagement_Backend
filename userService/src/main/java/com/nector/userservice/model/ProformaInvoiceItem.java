package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "proforma_invoice_items")
@Data
public class ProformaInvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "proforma_invoice_id", nullable = false)
    private ProformaInvoice proformaInvoice;
    
    private Integer srNo;
    private String description;
    private String hsnCode;
    private Integer quantity;
    private String unit = "Bags";
    private BigDecimal ratePerUnit;
    private BigDecimal amount;
}