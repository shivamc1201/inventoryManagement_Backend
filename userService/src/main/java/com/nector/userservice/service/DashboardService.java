package com.nector.userservice.service;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final SaleRepository saleRepository;
    
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String period) {
        log.info("Entering getDashboardData() with period: {}", period);
        
        if (period == null) period = "month";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = getStartDate(now, period);
        
        DashboardResponse.SalesMetrics metrics = getSalesMetrics(startDate, now);
        Map<String, BigDecimal> regionSales = getGroupedSales(
            saleRepository.getSalesByRegion(startDate, now)
        );
        Map<String, BigDecimal> categorySales = getGroupedSales(
            saleRepository.getSalesByCategory(startDate, now)
        );
        
        DashboardResponse response = new DashboardResponse();
        response.setYearToDate(metrics);
        response.setMonthToDate(metrics);
        response.setWeekToDate(metrics);
        response.setSalesByRegion(regionSales);
        response.setSalesByCategory(categorySales);
        
        log.info("Exiting getDashboardData() with response for period: {}", period);
        return response;
    }
    
    private DashboardResponse.SalesMetrics getSalesMetrics(LocalDateTime start, LocalDateTime end) {
        log.debug("Entering getSalesMetrics() from {} to {}", start, end);
        
        BigDecimal totalSales = saleRepository.getTotalSalesBetweenDates(start, end);
        Long transactionCount = saleRepository.getTransactionCountBetweenDates(start, end);
        
        // Handle null values
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        transactionCount = transactionCount != null ? transactionCount : 0L;
        
        DashboardResponse.SalesMetrics metrics = new DashboardResponse.SalesMetrics(totalSales, transactionCount);
        log.debug("Exiting getSalesMetrics() with totalSales: {}, transactionCount: {}", totalSales, transactionCount);
        return metrics;
    }
    
    private LocalDateTime getStartDate(LocalDateTime now, String period) {
        log.debug("Entering getStartDate() with period: {}", period);
        
        LocalDateTime startDate = switch (period.toLowerCase()) {
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "3months" -> now.minusMonths(3);
            case "6months" -> now.minusMonths(6);
            case "year" -> now.minusYears(1);
            case "all" -> LocalDateTime.of(2000, 1, 1, 0, 0); // All time data
            default -> LocalDateTime.of(2000, 1, 1, 0, 0); // Default: all time
        };
        
        log.debug("Exiting getStartDate() with startDate: {}", startDate);
        return startDate;
    }
    
    private Map<String, BigDecimal> getGroupedSales(List<Object[]> results) {
        log.debug("Entering getGroupedSales() with {} results", results.size());
        
        Map<String, BigDecimal> grouped = new HashMap<>();
        for (Object[] result : results) {
            String key = (String) result[0];
            BigDecimal value = (BigDecimal) result[1];
            grouped.put(key != null ? key : "Unknown", value);
        }
        
        log.debug("Exiting getGroupedSales() with {} grouped entries", grouped.size());
        return grouped;
    }
}