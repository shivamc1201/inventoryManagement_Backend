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
public class PendingDispatchRowDto {
    private Long id;
    private String challanNo;
    private String customer;
    private int items;
    private BigDecimal amount;
    private long readySinceDays;
    private LocalDateTime gdnDate;
}
