package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIssueRowDto {
    private Long id;
    private String itemType;
    private String transactionType;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private String unit;
    private String issuedTo;
    private String referenceNumber;
    private String comments;
    private BigDecimal quotedSellingPrice;
    private LocalDateTime createdAt;
}
