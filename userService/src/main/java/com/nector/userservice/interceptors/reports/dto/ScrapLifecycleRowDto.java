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
public class ScrapLifecycleRowDto {
    private Long id;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private BigDecimal quotedSellingPrice;
    private BigDecimal totalValue;
    private String issuedTo;
    private String approvalStatus;
    private LocalDateTime requestedOn;
    private LocalDateTime reviewedOn;
    private String reviewedBy;
    private String reviewComments;
}
