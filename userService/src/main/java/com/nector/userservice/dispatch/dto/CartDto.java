package com.nector.userservice.dispatch.dto;

import com.nector.userservice.model.Cart;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private Long distributorId;
    private String address;
    private Cart.CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDto> cartItems;
    private Long salespersonId;
    private String salespersonName;
}