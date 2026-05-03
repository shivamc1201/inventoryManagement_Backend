package com.nector.userservice.service;

import com.nector.userservice.dto.VolumeAnalyticsResponse;
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
public class VolumeAnalyticsService {

    private final OrderRepository orderRepository;
    private final SalesPersonRepository salesPersonRepository;

    @Transactional(readOnly = true)
    public VolumeAnalyticsResponse getVolumeAnalyticsData(String period, Long salespersonId, Long distributorId) {
        log.info("Entering getVolumeAnalyticsData() with period: {}, salespersonId: {}, distributorId: {}",
                period, salespersonId, distributorId);

        if (period == null) period = "all";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(now, period);

        VolumeAnalyticsResponse.VolumeMetrics monthToDate = getVolumeMetrics(startDate, now, salespersonId, distributorId);
        VolumeAnalyticsResponse.VolumeMetrics weekToDate = getVolumeMetrics(now.minusWeeks(1), now, salespersonId, distributorId);
        VolumeAnalyticsResponse.VolumeMetrics yearToDate = getVolumeMetrics(now.withDayOfYear(1), now, salespersonId, distributorId);

        Map<String, Long> regionVolume = getVolumeByRegion(startDate, now, salespersonId, distributorId);
        Map<String, Long> categoryVolume = getVolumeByCategory(startDate, now, salespersonId, distributorId);

        // Combined order summary (total GDN orders + amount for the selected period)
        Long totalOrders;
        BigDecimal totalAmount;
        if (salespersonId != null) {
            totalOrders = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, startDate, now);
            totalAmount = orderRepository.sumGdnAmountBySalespersonBetweenDates(salespersonId, startDate, now);
        } else if (distributorId != null) {
            totalOrders = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, startDate, now);
            totalAmount = orderRepository.sumGdnAmountByDistributorBetweenDates(distributorId, startDate, now);
        } else {
            totalOrders = orderRepository.countGdnOrdersBetweenDates(startDate, now);
            totalAmount = orderRepository.sumGdnAmountBetweenDates(startDate, now);
        }
        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;

        VolumeAnalyticsResponse response = new VolumeAnalyticsResponse();
        response.setYearToDate(yearToDate);
        response.setMonthToDate(monthToDate);
        response.setWeekToDate(weekToDate);
        response.setVolumeByRegion(regionVolume);
        response.setVolumeByCategory(categoryVolume);
        response.setTotalOrders(totalOrders);
        response.setTotalAmount(totalAmount);
        response.setPeriod(period);

        log.info("Exiting getVolumeAnalyticsData() with response for period: {}", period);
        return response;
    }

    private VolumeAnalyticsResponse.VolumeMetrics getVolumeMetrics(LocalDate start, LocalDate end, Long salespersonId, Long distributorId) {
        log.debug("Entering getVolumeMetrics() from {} to {}, salespersonId: {}, distributorId: {}",
                start, end, salespersonId, distributorId);

        Long totalTransactions;
        Long totalQuantity;

        // Only count GDN_GENERATED orders for volume analytics
        if (salespersonId != null) {
            totalTransactions = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, start, end);
            totalQuantity = getTotalQuantityBySalesperson(start, end, salespersonId);
        } else if (distributorId != null) {
            totalTransactions = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, start, end);
            totalQuantity = getTotalQuantityByDistributor(start, end, distributorId);
        } else {
            totalTransactions = orderRepository.countGdnOrdersBetweenDates(start, end);
            totalQuantity = getTotalQuantityBetweenDates(start, end);
        }

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

    private Map<String, Long> getVolumeByRegion(LocalDate startDate, LocalDate endDate, Long salespersonId, Long distributorId) {
        log.debug("Getting volume by region from {} to {}, salespersonId: {}, distributorId: {}",
                startDate, endDate, salespersonId, distributorId);

        // Only include GDN_GENERATED orders for volume by region
        List<com.nector.userservice.model.OrderWithSalesPerson> orders;

        if (salespersonId != null) {
            orders = orderRepository.findGdnOrdersBySalespersonAndCreatedAtBetween(salespersonId, startDate, endDate);
        } else if (distributorId != null) {
            orders = orderRepository.findGdnOrdersByDistributorAndCreatedAtBetween(distributorId, startDate, endDate);
        } else {
            orders = orderRepository.findGdnOrdersByCreatedAtBetween(startDate, endDate);
        }

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

    private Map<String, Long> getVolumeByCategory(LocalDate startDate, LocalDate endDate, Long salespersonId, Long distributorId) {
        log.debug("Getting volume by category from {} to {}, salespersonId: {}, distributorId: {}",
                startDate, endDate, salespersonId, distributorId);

        // Since carts table doesn't have product category, we'll return a placeholder
        // In a real implementation, this would come from order_items or products table
        Map<String, Long> categoryVolume = new HashMap<>();
        Long totalTransactions;

        // Only count GDN_GENERATED orders for category volume
        if (salespersonId != null) {
            totalTransactions = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, startDate, endDate);
        } else if (distributorId != null) {
            totalTransactions = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, startDate, endDate);
        } else {
            totalTransactions = orderRepository.countGdnOrdersBetweenDates(startDate, endDate);
        }

        categoryVolume.put("General", totalTransactions != null ? totalTransactions : 0L);
        return categoryVolume;
    }

    private Long getTotalQuantityBetweenDates(LocalDate start, LocalDate end) {
        log.debug("Getting total quantity between {} and {}", start, end);

        // Only count GDN_GENERATED orders
        var orders = orderRepository.findGdnOrdersByCreatedAtBetween(start, end);

        // For now, we'll count each order as 1 unit
        // In a real implementation, this would sum actual product quantities from order_items
        return (long) orders.size();
    }

    private Long getTotalQuantityBySalesperson(LocalDate start, LocalDate end, Long salespersonId) {
        log.debug("Getting total quantity for salesperson {} between {} and {}", salespersonId, start, end);

        // Only count GDN_GENERATED orders
        var orders = orderRepository.findGdnOrdersBySalespersonAndCreatedAtBetween(salespersonId, start, end);
        return (long) orders.size();
    }

    private Long getTotalQuantityByDistributor(LocalDate start, LocalDate end, Long distributorId) {
        log.debug("Getting total quantity for distributor {} between {} and {}", distributorId, start, end);

        // Only count GDN_GENERATED orders
        var orders = orderRepository.findGdnOrdersByDistributorAndCreatedAtBetween(distributorId, start, end);
        return (long) orders.size();
    }
}
