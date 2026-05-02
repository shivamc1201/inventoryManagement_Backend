package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.model.MachinePart;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MachinePartRequest {

    private String name;

    private String partNumber;

    private MachinePart.Category category;

    private String vendor;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    private Integer quantity;

    private String unit;

    private MachinePart.Condition condition;

    private String hsn;

    private String taxRateCode;

    private ProductStatus status;
}