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

    public List<OrderResponse> getAllOrders() {
        List<OrderWithSalesPerson> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersWithFilters(OrderStatus status, Long salespersonId,
                                                   Long distributorId, LocalDate dateFrom, LocalDate dateTo) {
        List<OrderWithSalesPerson> orders = orderRepository.findWithFilters(status, salespersonId, distributorId, dateFrom, dateTo);
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
        response.setDistributorId(order.getDistributorId());
        response.setAddress(order.getAddress());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCreatedBy(order.getCreatedBy());
        return response;
    }
}
