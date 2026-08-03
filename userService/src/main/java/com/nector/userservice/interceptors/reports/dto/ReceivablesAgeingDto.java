package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivablesAgeingDto {
    private Long dealerId;
    private String dealerName;
    private BigDecimal currentBalance;
    private BigDecimal bucket0to30;
    private BigDecimal bucket31to60;
    private BigDecimal bucket61to90;
    private BigDecimal bucket90plus;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
}
