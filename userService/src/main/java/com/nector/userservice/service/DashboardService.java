package com.nector.userservice.service;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.repository.OrderRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final OrderRepository orderRepository;
    private final SalesPersonRepository salesPersonRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String period) {
        log.info("Entering getDashboardData() with period: {}", period);

        if (period == null) period = "month";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(now, period);

        DashboardResponse.SalesMetrics monthToDate = getSalesMetrics(startDate, now);
        DashboardResponse.SalesMetrics weekToDate = getSalesMetrics(now.minusWeeks(1), now);
        DashboardResponse.SalesMetrics yearToDate = getSalesMetrics(now.withDayOfYear(1), now);

        Map<String, BigDecimal> regionSales = getSalesByRegion(startDate, now);
        Map<String, BigDecimal> categorySales = getSalesByCategory(startDate, now);

        DashboardResponse response = new DashboardResponse();
        response.setYearToDate(yearToDate);
        response.setMonthToDate(monthToDate);
        response.setWeekToDate(weekToDate);
        response.setSalesByRegion(regionSales);
        response.setSalesByCategory(categorySales);

        log.info("Exiting getDashboardData() with response for period: {}", period);
        return response;
    }

    private DashboardResponse.SalesMetrics getSalesMetrics(LocalDate start, LocalDate end) {
        log.debug("Entering getSalesMetrics() from {} to {}", start, end);

        BigDecimal totalSales = orderRepository.getTotalAmountBetweenDates(start, end);
        Long transactionCount = orderRepository.countOrdersBetweenDates(start, end);

        // Handle null values
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        transactionCount = transactionCount != null ? transactionCount : 0L;

        DashboardResponse.SalesMetrics metrics = new DashboardResponse.SalesMetrics(totalSales, transactionCount);
        log.debug("Exiting getSalesMetrics() with totalSales: {}, transactionCount: {}", totalSales, transactionCount);
        return metrics;
    }

    private LocalDate getStartDate(LocalDate now, String period) {
        log.debug("Entering getStartDate() with period: {}", period);

        LocalDate startDate = switch (period.toLowerCase()) {
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "3months" -> now.minusMonths(3);
            case "6months" -> now.minusMonths(6);
            case "year" -> now.minusYears(1);
            case "all" -> LocalDate.of(2000, 1, 1); // All time data
            default -> LocalDate.of(2000, 1, 1); // Default: all time
        };

        log.debug("Exiting getStartDate() with startDate: {}", startDate);
        return startDate;
    }

    private Map<String, BigDecimal> getSalesByRegion(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting sales by region from {} to {}", startDate, endDate);

        // Get all orders in the date range and group by salesperson region
        var orders = orderRepository.findByCreatedAtBetween(startDate, endDate);

        return orders.stream()
                .filter(order -> order.getSalespersonId() != null)
                .collect(Collectors.groupingBy(
                        order -> {
                            var salesPerson = salesPersonRepository.findById(order.getSalespersonId()).orElse(null);
                            return salesPerson != null ? salesPerson.getZone() : "Unknown";
                        },
                        Collectors.mapping(
                                order -> order.getTotalCartAmount() != null ? order.getTotalCartAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    private Map<String, BigDecimal> getSalesByCategory(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting sales by category from {} to {}", startDate, endDate);

        // Since carts table doesn't have product category, we'll return a placeholder
        // In a real implementation, this would come from order_items or products table
        Map<String, BigDecimal> categorySales = new HashMap<>();
        categorySales.put("General", orderRepository.getTotalAmountBetweenDates(startDate, endDate));
        return categorySales;
    }
}