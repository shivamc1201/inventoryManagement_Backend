package com.nector.userservice.dto;

import com.nector.userservice.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long distributorId;
    private String address;
    private OrderStatus status;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Long createdBy;
}
