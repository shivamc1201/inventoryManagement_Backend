package com.nector.userservice.interceptors.products.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DamageRequest {

    private BigDecimal quantity;

    private String reason;

    private String notes;

    private String damagedBy;
}
