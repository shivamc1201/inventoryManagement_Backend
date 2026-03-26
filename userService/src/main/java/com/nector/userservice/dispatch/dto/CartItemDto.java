package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long id;
    private String itemSku;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
}