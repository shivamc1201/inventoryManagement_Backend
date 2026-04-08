package com.nector.userservice.service;

import com.nector.userservice.dto.OrderDashboardResponse;
import com.nector.userservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorDashboardService {
    
    private final OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public OrderDashboardResponse getDistributorOrderDashboardData(String period, Long distributorId) {
        log.info("Entering getDistributorOrderDashboardData() with period: {}, distributorId: {}", period, distributorId);
        
        if (period == null) period = "month";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDateForOrders(now, period);
        
        Long totalOrders;
        BigDecimal totalAmount;

        if (distributorId != null) {
            // Filter by specific distributor - prioritize distributorId over date filtering
            // Get all orders for this distributor regardless of date when distributorId is provided
            totalOrders = orderRepository.countOrdersByDistributor(distributorId);
            totalAmount = orderRepository.getTotalAmountByDistributor(distributorId);
        } else {
            // Get all orders with date filtering (only when no distributorId is specified)
            totalOrders = orderRepository.countOrdersBetweenDates(startDate, now);
            totalAmount = orderRepository.getTotalAmountBetweenDates(startDate, now);
        }
        
        // Handle null values
        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        
        OrderDashboardResponse response = new OrderDashboardResponse(totalOrders, totalAmount, period);
        
        log.info("Exiting getDistributorOrderDashboardData() with totalOrders: {}, totalAmount: {}", totalOrders, totalAmount);
        return response;
    }
    
    private LocalDate getStartDateForOrders(LocalDate now, String period) {
        log.debug("Entering getStartDateForOrders() with period: {}", period);
        
        LocalDate startDate = switch (period.toLowerCase()) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            case "all" -> LocalDate.of(2000, 1, 1); // All-time data
            default -> now.minusMonths(1); // Default: month
        };
        
        log.debug("Exiting getStartDateForOrders() with startDate: {}", startDate);
        return startDate;
    }
}
