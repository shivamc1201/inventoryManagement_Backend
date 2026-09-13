package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_line_items", indexes = {
        @Index(name = "idx_ili_invoice_id", columnList = "invoice_id"),
        @Index(name = "idx_ili_product_id", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "rate_per_unit", nullable = false, precision = 15, scale = 2)
    private BigDecimal ratePerUnit;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "unit")
    private String unit;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
