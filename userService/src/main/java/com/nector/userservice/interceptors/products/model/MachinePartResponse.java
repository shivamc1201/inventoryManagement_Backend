package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.model.MachinePart;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MachinePartResponse {
    private Long id;
    private String name;
    private String partNumber;
    private MachinePart.Category category;
    private String vendor;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private Integer quantity;
    private MachinePart.Condition condition;
    private String hsn;
    private BigDecimal taxRate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProductStatus status;
}