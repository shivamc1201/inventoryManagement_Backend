package com.nector.userservice.dispatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "gdn_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GdnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gdn_id", nullable = false)
    private Gdn gdn;
    
    @Column(nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private String itemDescription;
    
    @Column(nullable = false)
    private String unitType = "KG";
    
    @Column(nullable = false)
    private Integer noOfUnitsDispatch;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal weightPerUnit;
}