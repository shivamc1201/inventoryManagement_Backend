package com.nector.userservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerOrderRequest {

    private Long dealerId;

    private Long itemId;

    private String sku;

    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    private String itemName;

    private Integer quantity;

    private BigDecimal amount;

    private LocalDate date;
}
