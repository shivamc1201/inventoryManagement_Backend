package com.nector.userservice.service;

import com.nector.userservice.dto.OrderResponse;
import com.nector.userservice.dto.OrderSummaryResponse;
import com.nector.userservice.enums.OrderStatus;
import com.nector.userservice.model.OrderWithSalesPerson;
import com.nector.userservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final SalesPersonService salesPersonService;

    public List<OrderResponse> getAllOrders() {
        log.info("Entering getAllOrders()");
        List<OrderWithSalesPerson> orders = orderRepository.findAll();
        List<OrderResponse> result = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        log.info("Exiting getAllOrders() - returned {} orders", result.size());
        return result;
    }

    public List<OrderResponse> getOrdersWithFilters(OrderStatus status, Long salespersonId,
                                                   Long distributorId, LocalDate dateFrom, LocalDate dateTo) {
        log.info("Entering getOrdersWithFilters() - status: {}, salespersonId: {}, distributorId: {}, dateFrom: {}, dateTo: {}", status, salespersonId, distributorId, dateFrom, dateTo);
        LocalDateTime dateFromTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dateToTime = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        List<OrderWithSalesPerson> orders = orderRepository.findWithFilters(status, salespersonId, distributorId, dateFromTime, dateToTime);
        List<OrderResponse> result = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        log.info("Exiting getOrdersWithFilters() - returned {} orders", result.size());
        return result;
    }

    public List<OrderResponse> getOrdersByHierarchy(Long salespersonId, OrderStatus status, 
                                                   LocalDate dateFrom, LocalDate dateTo) {
        log.info("Getting orders for salesperson ID {} and their subordinates", salespersonId);
        
        // Get all subordinate IDs
        List<Long> subordinateIds = salesPersonService.getSubordinateIds(salespersonId);
        
        // Include the salesperson themselves
        List<Long> allSalespersonIds = new java.util.ArrayList<>();
        allSalespersonIds.add(salespersonId);
        allSalespersonIds.addAll(subordinateIds);
        
        log.info("Getting orders for salesperson IDs: {}", allSalespersonIds);
        
        // Get orders for all these salespersons
        LocalDateTime dateFromTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime dateToTime = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        List<OrderWithSalesPerson> orders = orderRepository.findBySalespersonIds(allSalespersonIds, status, dateFromTime, dateToTime);
        
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrderSummaryResponse getOrderSummary(OrderStatus status) {
        log.info("Getting order summary with filters - status: {}", status);

        long totalOrders = orderRepository.countWithFilters(status);

        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setTotalOrders(totalOrders);

        return response;
    }

    private OrderResponse convertToResponse(OrderWithSalesPerson order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getCreatedBy());
        response.setDistributorId(order.getDistributorId());
        response.setAddress(order.getAddress());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCreatedBy(order.getCreatedBy());
        
        // Additional fields from OrderWithSalesPerson
        response.setSalespersonId(order.getSalespersonId());
        response.setSalespersonName(order.getSalespersonName());
        response.setDistributorName(order.getDistributorName());
        response.setTotalCartAmount(order.getTotalCartAmount());
        
        return response;
    }
}
