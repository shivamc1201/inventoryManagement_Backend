package com.nector.userservice.service;

import com.nector.userservice.dto.OrderDashboardResponse;
import com.nector.userservice.enums.OrderStatus;
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
            // Filter by specific distributor - only count GDN_GENERATED orders for this distributor
            totalOrders = orderRepository.countOrdersByDistributorAndStatus(distributorId, OrderStatus.GDN_GENERATED);
            totalAmount = orderRepository.getTotalAmountByDistributorAndStatus(distributorId, OrderStatus.GDN_GENERATED);
        } else {
            // Get all GDN_GENERATED orders with date filtering (only when no distributorId is specified)
            totalOrders = orderRepository.countOrdersByStatusBetweenDates(OrderStatus.GDN_GENERATED, startDate, now);
            totalAmount = orderRepository.getTotalAmountByStatusBetweenDates(OrderStatus.GDN_GENERATED, startDate, now);
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
