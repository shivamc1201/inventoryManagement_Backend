package com.nector.userservice.interceptors.products.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class DamageResponse {

    private Long id;

    private Long rawMaterialId;

    private String rawMaterialName;

    private String materialCode;

    private BigDecimal quantityDamaged;

    private BigDecimal quantityBefore;

    private BigDecimal quantityAfter;

    private String reason;

    private String notes;

    private String damagedBy;

    private Instant damagedAt;
}
