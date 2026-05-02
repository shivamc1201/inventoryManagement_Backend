package com.nector.userservice.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingDTO {
    private Long id;               // order_tracking.id (Order Tracking ID)
    private Long cartId;           // order_tracking.cart_id (Original Cart ID)
    private String orderNumber;
    private String distributorName;
    private Long distributorId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private BigDecimal totalAmount;
    private List<OrderStepDTO> steps;   // always 11 items
}
