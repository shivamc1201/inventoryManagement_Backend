package com.nector.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesResponse {
    private String financialYear;
    private List<MonthlyData> months;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String month;
        private int year;
        private BigDecimal revenue;
        private long orderCount;
    }
}
