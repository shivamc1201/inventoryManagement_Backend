package com.nector.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "sku")
    private String sku;

    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    @Column(name = "item_name")
    private String itemName;

    @Column
    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
