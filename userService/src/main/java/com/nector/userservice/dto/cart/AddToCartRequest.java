package com.nector.userservice.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddToCartRequest {
    
    @NotNull(message = "Item ID is required")
    private String itemId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "SKU is required")
    private String sku;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String imageUrl;

    private Boolean active;
}