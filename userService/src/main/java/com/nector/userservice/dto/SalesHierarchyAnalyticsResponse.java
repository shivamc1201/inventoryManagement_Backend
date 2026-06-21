package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesHierarchyAnalyticsResponse {

    private Long salesPersonId;
    private String salesPersonName;
    private String role;

    // Current month (MTD)
    private BigDecimal volumeMTD;
    private Long transactionsMTD;
    private BigDecimal valueMTD;

    // Current year (YTD)
    private BigDecimal volumeYTD;
    private Long transactionsYTD;
    private BigDecimal valueYTD;

    // Total team members whose data is included (self + all subordinates)
    private Integer teamSize;
}
