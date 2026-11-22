package com.nector.userservice.controller;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.dto.MonthlySalesResponse;
import com.nector.userservice.dto.OrderDashboardResponse;
import com.nector.userservice.dto.SalesHierarchyAnalyticsResponse;
import com.nector.userservice.dto.VolumeAnalyticsResponse;
import com.nector.userservice.enums.OrderStatus;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.OrderRepository;
import com.nector.userservice.service.DashboardService;
import com.nector.userservice.service.DistributorDashboardService;
import com.nector.userservice.service.UserService;
import com.nector.userservice.service.VolumeAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "APIs for dashboard analytics and metrics")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final DistributorDashboardService distributorDashboardService;
    private final VolumeAnalyticsService volumeAnalyticsService;
    
    @GetMapping("/analytics")
    @Operation(summary = "Get dashboard analytics", description = "Retrieves dashboard analytics data for specified time period")
    @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully")
    public ResponseEntity<DashboardResponse> getDashboardAnalytics(
            @RequestParam(required = false) String period) {
        log.info("Entering getDashboardAnalytics() with period: {}", period);
        DashboardResponse response = dashboardService.getDashboardData(period);
        log.info("Exiting getDashboardAnalytics() with period: {}", period);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volume-analytics")
    @Operation(summary = "Get volume analytics", description = "Retrieves volume-based analytics data for specified time period. Optional salespersonId and distributorId for filtering.")
    @ApiResponse(responseCode = "200", description = "Volume analytics data retrieved successfully")
    public ResponseEntity<VolumeAnalyticsResponse> getVolumeAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) Long distributorId) {
        log.info("Entering getVolumeAnalytics() with period: {}, salespersonId: {}, distributorId: {}", period, salespersonId, distributorId);
        VolumeAnalyticsResponse response = volumeAnalyticsService.getVolumeAnalyticsData(period, salespersonId, distributorId);
        log.info("Exiting getVolumeAnalytics() with period: {}", period);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-hierarchy-analytics")
    @Operation(summary = "Get hierarchy-based sales analytics", description = "Returns MTD/YTD volume (tons) and value (amount) aggregated across the salesperson and all subordinates in their hierarchy.")
    @ApiResponse(responseCode = "200", description = "Hierarchy analytics retrieved successfully")
    public ResponseEntity<SalesHierarchyAnalyticsResponse> getSalesHierarchyAnalytics(
            @RequestParam Long salesPersonId) {
        log.info("Entering getSalesHierarchyAnalytics() with salesPersonId: {}", salesPersonId);
        SalesHierarchyAnalyticsResponse response = volumeAnalyticsService.getHierarchyAnalytics(salesPersonId);
        log.info("Exiting getSalesHierarchyAnalytics() with salesPersonId: {}", salesPersonId);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/userprofile")
    @Operation(summary = "Get user profile", description = "Retrieves user profile data for the logged-in user")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    public ResponseEntity<User> getUserProfile(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        log.info("Entering getUserProfile() for username: {}", username);
        User user = userService.getUserByUsername(username);
        log.info("Exiting getUserProfile() for username: {}", username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get order dashboard analytics", description = "Retrieves order analytics data for specified time period")
    @ApiResponse(responseCode = "200", description = "Order analytics data retrieved successfully")
    public ResponseEntity<OrderDashboardResponse> getOrderDashboard(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long salespersonId) {
        log.info("Entering getOrderDashboard() with period: {}, salespersonId: {}", period, salespersonId);
        if (period == null) period = "month";
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDateForOrders(now, period);

        Long totalOrders;
        BigDecimal totalAmount;

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = now.atTime(LocalTime.MAX);

        if (salespersonId != null) {
            // Filter by specific salesperson - only count GDN_GENERATED orders
            totalOrders = orderRepository.countOrdersBySalespersonAndStatusBetweenDates(salespersonId, OrderStatus.GDN_GENERATED, startDateTime, endDateTime);
            totalAmount = orderRepository.getTotalAmountBySalespersonAndStatusBetweenDates(salespersonId, OrderStatus.GDN_GENERATED, startDateTime, endDateTime);
        } else {
            // Get all GDN_GENERATED orders
            totalOrders = orderRepository.countOrdersByStatusBetweenDates(OrderStatus.GDN_GENERATED, startDateTime, endDateTime);
            totalAmount = orderRepository.getTotalAmountByStatusBetweenDates(OrderStatus.GDN_GENERATED, startDateTime, endDateTime);
        }

        // Handle null values
        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;

        OrderDashboardResponse response = new OrderDashboardResponse(totalOrders, totalAmount, period);
        log.info("Exiting getOrderDashboard() - totalOrders: {}, period: {}", totalOrders, period);
        return ResponseEntity.ok(response);
    }

    private LocalDate getStartDateForOrders(LocalDate now, String period) {
        return switch (period.toLowerCase()) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            case "all" -> LocalDate.of(2000, 1, 1); // All-time data
            default -> now.minusMonths(1); // Default: month
        };
    }


    @GetMapping("/monthly-sales")
    @Operation(summary = "Get monthly sales revenue", description = "Retrieves monthly revenue and order count for GDN_GENERATED orders across the current Indian financial year (Apr–Mar)")
    @ApiResponse(responseCode = "200", description = "Monthly sales data retrieved successfully")
    public ResponseEntity<MonthlySalesResponse> getMonthlySales() {
        log.info("Entering getMonthlySales()");
        MonthlySalesResponse response = dashboardService.getMonthlySalesData();
        log.info("Exiting getMonthlySales()");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/distributor-orders")
    @Operation(summary = "Get distributor order dashboard analytics", description = "Retrieves order analytics data for specified time period and distributor")
    @ApiResponse(responseCode = "200", description = "Distributor order analytics data retrieved successfully")
    public ResponseEntity<OrderDashboardResponse> getDistributorOrderDashboard(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) Long distributorId) {
        log.info("Entering getDistributorOrderDashboard() with period: {}, distributorId: {}", period, distributorId);
        OrderDashboardResponse response = distributorDashboardService.getDistributorOrderDashboardData(period, distributorId);
        log.info("Exiting getDistributorOrderDashboard() with period: {}, distributorId: {}", period, distributorId);
        return ResponseEntity.ok(response);
    }

}

