package com.nector.userservice.dispatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gdn")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gdn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String gdnNumber;
    
    @Column(nullable = false)
    private Long orderId;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime gdnDate;
    
    private String dispatchFromAddress;
    private String shippingAddress;
    private String deliveryMethod = "By Road";
    private String vehicleNo;
    private String transportName;
    private String driverName;
    private String driverMobile;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalWeight;
    
    private Integer totalPackages;
    
    @OneToMany(mappedBy = "gdn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GdnItem> gdnItems = new ArrayList<>();
}