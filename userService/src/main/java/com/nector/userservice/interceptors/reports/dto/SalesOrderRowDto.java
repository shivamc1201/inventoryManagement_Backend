package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderRowDto {
    private Long id;
    private String orderNumber;
    private Long distributorId;
    private String distributorName;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String currentStatus;
}
