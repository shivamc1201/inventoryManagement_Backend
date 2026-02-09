package com.nector.userservice.dto.sales;

import com.nector.userservice.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesResponse {

    private Long id;
    private String fullName;
    private String emailAddress;
    private String phoneNumber;
    private String subject;
    private String description;
    private SalesStatus status;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String customerName;
    private LocalDateTime saleDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
