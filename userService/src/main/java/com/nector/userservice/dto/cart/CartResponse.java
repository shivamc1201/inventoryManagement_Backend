package com.nector.userservice.dto.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private String status;
    private List<CartItemResponse> cartItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long salespersonId;
    private String salespersonName;
    private Long distributorId;
    private String distributorName;
    private String distributorAddress;
    private java.math.BigDecimal totalCartAmount;
    private String dismissReason;

//    private BigDecimal totalWeight;
    private BigDecimal volumeInTons;
    private BigDecimal totalCartWeightKg;
}
