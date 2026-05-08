package com.nector.userservice.service;

import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.dto.VolumeAnalyticsResponse;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.repository.OrderRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolumeAnalyticsService {

    private final OrderRepository orderRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final GdnRepository gdnRepository;
    private final DistributorRepository distributorRepository;

    @Transactional(readOnly = true)
    public VolumeAnalyticsResponse getVolumeAnalyticsData(String period, Long salespersonId, Long distributorId) {
        log.info("Entering getVolumeAnalyticsData() with period: {}, salespersonId: {}, distributorId: {}",
                period, salespersonId, distributorId);

        if (period == null) period = "all";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(now, period);

        VolumeAnalyticsResponse.VolumeMetrics monthToDate = getVolumeMetrics(startDate, now, salespersonId, distributorId);
        VolumeAnalyticsResponse.VolumeMetrics yearToDate = getVolumeMetrics(now.withDayOfYear(1), now, salespersonId, distributorId);

        Map<String, Long> regionVolume = getVolumeByRegion(startDate, now, salespersonId, distributorId);
        Map<String, Long> categoryVolume = getVolumeByCategory(startDate, now, salespersonId, distributorId);

        // Combined order summary (total GDN orders + amount for the selected period)
        Long totalOrders;
        BigDecimal totalAmount;
        if (salespersonId != null) {
            totalOrders = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, toDateTime(startDate), toEndOfDay(now));
            totalAmount = orderRepository.sumGdnAmountBySalespersonBetweenDates(salespersonId, toDateTime(startDate), toEndOfDay(now));
        } else if (distributorId != null) {
            totalOrders = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, toDateTime(startDate), toEndOfDay(now));
            totalAmount = orderRepository.sumGdnAmountByDistributorBetweenDates(distributorId, toDateTime(startDate), toEndOfDay(now));
        } else {
            totalOrders = orderRepository.countGdnOrdersBetweenDates(toDateTime(startDate), toEndOfDay(now));
            totalAmount = orderRepository.sumGdnAmountBetweenDates(toDateTime(startDate), toEndOfDay(now));
        }
        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;

        // Monthly total (current month MTD)
        LocalDate monthStart = now.withDayOfMonth(1);
        BigDecimal totalAmountMonthly;
        if (salespersonId != null) {
            totalAmountMonthly = orderRepository.sumGdnAmountBySalespersonBetweenDates(salespersonId, toDateTime(monthStart), toEndOfDay(now));
        } else if (distributorId != null) {
            totalAmountMonthly = orderRepository.sumGdnAmountByDistributorBetweenDates(distributorId, toDateTime(monthStart), toEndOfDay(now));
        } else {
            totalAmountMonthly = orderRepository.sumGdnAmountBetweenDates(toDateTime(monthStart), toEndOfDay(now));
        }
        totalAmountMonthly = totalAmountMonthly != null ? totalAmountMonthly : BigDecimal.ZERO;

        // Yearly total (current year YTD)
        LocalDate yearStart = now.withDayOfYear(1);
        BigDecimal totalAmountYearly;
        if (salespersonId != null) {
            totalAmountYearly = orderRepository.sumGdnAmountBySalespersonBetweenDates(salespersonId, toDateTime(yearStart), toEndOfDay(now));
        } else if (distributorId != null) {
            totalAmountYearly = orderRepository.sumGdnAmountByDistributorBetweenDates(distributorId, toDateTime(yearStart), toEndOfDay(now));
        } else {
            totalAmountYearly = orderRepository.sumGdnAmountBetweenDates(toDateTime(yearStart), toEndOfDay(now));
        }
        totalAmountYearly = totalAmountYearly != null ? totalAmountYearly : BigDecimal.ZERO;

        VolumeAnalyticsResponse response = new VolumeAnalyticsResponse();
        response.setYearToDate(yearToDate);
        response.setMonthToDate(monthToDate);
        response.setVolumeByRegion(regionVolume);
        response.setVolumeByCategory(categoryVolume);
        response.setTotalOrders(totalOrders);
        response.setTotalAmount(totalAmount);
        response.setTotalAmountMonthly(totalAmountMonthly);
        response.setTotalAmountYearly(totalAmountYearly);
        response.setPeriod(period);

        // Compute totalOutstanding = creditLimit - creditBalance for the given distributor
        if (distributorId != null) {
            distributorRepository.findById(distributorId).ifPresent(distributor -> {
                BigDecimal creditLimit = distributor.getCreditLimit() != null ? distributor.getCreditLimit() : BigDecimal.ZERO;
                BigDecimal creditBalance = distributor.getCreditBalance() != null ? distributor.getCreditBalance() : BigDecimal.ZERO;
                response.setTotalOutstanding(creditLimit.subtract(creditBalance));
            });
        }

        log.info("Exiting getVolumeAnalyticsData() with response for period: {}", period);
        return response;
    }

    private VolumeAnalyticsResponse.VolumeMetrics getVolumeMetrics(LocalDate start, LocalDate end, Long salespersonId, Long distributorId) {
        log.debug("Entering getVolumeMetrics() from {} to {}, salespersonId: {}, distributorId: {}",
                start, end, salespersonId, distributorId);

        Long totalTransactions;
        BigDecimal totalVolumeKg;

        // Only count GDN_GENERATED orders for volume analytics
        if (salespersonId != null) {
            totalTransactions = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, toDateTime(start), toEndOfDay(end));
            totalVolumeKg = getTotalWeightKgBySalesperson(start, end, salespersonId);
        } else if (distributorId != null) {
            totalTransactions = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, toDateTime(start), toEndOfDay(end));
            totalVolumeKg = getTotalWeightKgByDistributor(start, end, distributorId);
        } else {
            totalTransactions = orderRepository.countGdnOrdersBetweenDates(toDateTime(start), toEndOfDay(end));
            totalVolumeKg = getTotalWeightKgBetweenDates(start, end);
        }

        // Handle null values
        totalTransactions = totalTransactions != null ? totalTransactions : 0L;
        totalVolumeKg = totalVolumeKg != null ? totalVolumeKg : BigDecimal.ZERO;

        VolumeAnalyticsResponse.VolumeMetrics metrics = new VolumeAnalyticsResponse.VolumeMetrics(totalTransactions, totalVolumeKg);
        log.debug("Exiting getVolumeMetrics() with totalTransactions: {}, totalVolumeKg: {}", totalTransactions, totalVolumeKg);
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
            orders = orderRepository.findGdnOrdersBySalespersonAndCreatedAtBetween(salespersonId, toDateTime(startDate), toEndOfDay(endDate));
        } else if (distributorId != null) {
            orders = orderRepository.findGdnOrdersByDistributorAndCreatedAtBetween(distributorId, toDateTime(startDate), toEndOfDay(endDate));
        } else {
            orders = orderRepository.findGdnOrdersByCreatedAtBetween(toDateTime(startDate), toEndOfDay(endDate));
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
            totalTransactions = orderRepository.countGdnOrdersBySalespersonBetweenDates(salespersonId, toDateTime(startDate), toEndOfDay(endDate));
        } else if (distributorId != null) {
            totalTransactions = orderRepository.countGdnOrdersByDistributorBetweenDates(distributorId, toDateTime(startDate), toEndOfDay(endDate));
        } else {
            totalTransactions = orderRepository.countGdnOrdersBetweenDates(toDateTime(startDate), toEndOfDay(endDate));
        }

        categoryVolume.put("General", totalTransactions != null ? totalTransactions : 0L);
        return categoryVolume;
    }

    private BigDecimal getTotalWeightKgBetweenDates(LocalDate start, LocalDate end) {
        log.debug("Getting total weight between {} and {}", start, end);

        // Get GDN_GENERATED orders and sum their totalWeight from GDN records
        var orders = orderRepository.findGdnOrdersByCreatedAtBetween(toDateTime(start), toEndOfDay(end));

        return orders.stream()
                .map(order -> gdnRepository.findByOrderId(order.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(gdn -> gdn.getTotalWeight() != null ? gdn.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalWeightKgBySalesperson(LocalDate start, LocalDate end, Long salespersonId) {
        log.debug("Getting total weight for salesperson {} between {} and {}", salespersonId, start, end);

        var orders = orderRepository.findGdnOrdersBySalespersonAndCreatedAtBetween(salespersonId, toDateTime(start), toEndOfDay(end));

        return orders.stream()
                .map(order -> gdnRepository.findByOrderId(order.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(gdn -> gdn.getTotalWeight() != null ? gdn.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalWeightKgByDistributor(LocalDate start, LocalDate end, Long distributorId) {
        log.debug("Getting total weight for distributor {} between {} and {}", distributorId, start, end);

        var orders = orderRepository.findGdnOrdersByDistributorAndCreatedAtBetween(distributorId, toDateTime(start), toEndOfDay(end));

        return orders.stream()
                .map(order -> gdnRepository.findByOrderId(order.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(gdn -> gdn.getTotalWeight() != null ? gdn.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDateTime toDateTime(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}
