package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLifecycleDto {
    private String batchNumber;
    private String productName;
    private String sku;
    private Integer quantity;
    private LocalDate expiryDate;
    private boolean expired;
    private LocalDateTime createdAt;
}
