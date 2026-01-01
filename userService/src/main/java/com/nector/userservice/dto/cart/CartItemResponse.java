package com.nector.userservice.dto.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private String itemSku;
    private Integer quantity;
    private BigDecimal priceAtTime;
    private BigDecimal totalPrice;
}