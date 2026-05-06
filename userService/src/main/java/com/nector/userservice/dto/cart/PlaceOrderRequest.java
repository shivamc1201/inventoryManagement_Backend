package com.nector.userservice.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Address is required")
    private String address;

    private String deliveryBy;
}