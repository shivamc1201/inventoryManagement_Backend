package com.nector.userservice.service;

import com.nector.userservice.dto.VolumeAnalyticsResponse;
import com.nector.userservice.repository.OrderRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolumeAnalyticsService {

    private final OrderRepository orderRepository;
    private final SalesPersonRepository salesPersonRepository;

    @Transactional(readOnly = true)
    public VolumeAnalyticsResponse getVolumeAnalyticsData(String period) {
        log.info("Entering getVolumeAnalyticsData() with period: {}", period);

        if (period == null) period = "month";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(now, period);

        VolumeAnalyticsResponse.VolumeMetrics monthToDate = getVolumeMetrics(startDate, now);
        VolumeAnalyticsResponse.VolumeMetrics weekToDate = getVolumeMetrics(now.minusWeeks(1), now);
        VolumeAnalyticsResponse.VolumeMetrics yearToDate = getVolumeMetrics(now.withDayOfYear(1), now);

        Map<String, Long> regionVolume = getVolumeByRegion(startDate, now);
        Map<String, Long> categoryVolume = getVolumeByCategory(startDate, now);

        VolumeAnalyticsResponse response = new VolumeAnalyticsResponse();
        response.setYearToDate(yearToDate);
        response.setMonthToDate(monthToDate);
        response.setWeekToDate(weekToDate);
        response.setVolumeByRegion(regionVolume);
        response.setVolumeByCategory(categoryVolume);

        log.info("Exiting getVolumeAnalyticsData() with response for period: {}", period);
        return response;
    }

    private VolumeAnalyticsResponse.VolumeMetrics getVolumeMetrics(LocalDate start, LocalDate end) {
        log.debug("Entering getVolumeMetrics() from {} to {}", start, end);

        Long totalTransactions = orderRepository.countOrdersBetweenDates(start, end);
        Long totalQuantity = getTotalQuantityBetweenDates(start, end);

        // Handle null values
        totalTransactions = totalTransactions != null ? totalTransactions : 0L;
        totalQuantity = totalQuantity != null ? totalQuantity : 0L;

        VolumeAnalyticsResponse.VolumeMetrics metrics = new VolumeAnalyticsResponse.VolumeMetrics(totalTransactions, totalQuantity);
        log.debug("Exiting getVolumeMetrics() with totalTransactions: {}, totalQuantity: {}", totalTransactions, totalQuantity);
        return metrics;
    }

    private LocalDate getStartDate(LocalDate now, String period) {
        log.debug("Entering getStartDate() with period: {}", period);

        LocalDate startDate = switch (period != null ? period.toLowerCase() : "all") {
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

    private Map<String, Long> getVolumeByRegion(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting volume by region from {} to {}", startDate, endDate);

        // Get all orders in the date range and group by salesperson region
        var orders = orderRepository.findByCreatedAtBetween(startDate, endDate);

        return orders.stream()
                .filter(order -> order.getSalespersonId() != null)
                .collect(Collectors.groupingBy(
                        order -> {
                            var salesPerson = salesPersonRepository.findById(order.getSalespersonId()).orElse(null);
                            if (salesPerson == null) {
                                return "Unknown";
                            }
                            // Handle null zone by providing a default value
                            String zone = salesPerson.getZone();
                            return zone != null && !zone.trim().isEmpty() ? zone : "Unassigned";
                        },
                        Collectors.counting()
                ));
    }

    private Map<String, Long> getVolumeByCategory(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting volume by category from {} to {}", startDate, endDate);

        // Since carts table doesn't have product category, we'll return a placeholder
        // In a real implementation, this would come from order_items or products table
        Map<String, Long> categoryVolume = new HashMap<>();
        Long totalTransactions = orderRepository.countOrdersBetweenDates(startDate, endDate);
        categoryVolume.put("General", totalTransactions != null ? totalTransactions : 0L);
        return categoryVolume;
    }

    private Long getTotalQuantityBetweenDates(LocalDate start, LocalDate end) {
        log.debug("Getting total quantity between {} and {}", start, end);
        
        // Get all orders and sum their quantities
        var orders = orderRepository.findByCreatedAtBetween(start, end);
        
        // For now, we'll count each order as 1 unit
        // In a real implementation, this would sum actual product quantities from order_items
        return (long) orders.size();
    }
}
