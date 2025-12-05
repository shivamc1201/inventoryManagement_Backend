package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column
    private String region;
    
    @Column(name = "product_category")
    private String productCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private User salesPerson;
    
    @PrePersist
    protected void onCreate() {
        if (saleDate == null) {
            saleDate = LocalDateTime.now();
        }
    }
}