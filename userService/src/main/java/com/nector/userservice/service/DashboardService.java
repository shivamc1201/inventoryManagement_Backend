package com.nector.userservice.service;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.dto.MonthlySalesResponse;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.repository.DealerRepository;
import com.nector.userservice.repository.OrderRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    private final DealerRepository dealerRepository;

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

        // Get total GDN orders and amount for the selected period
        Long totalOrders = orderRepository.countGdnOrdersBetweenDates(startDate.atStartOfDay(), now.atTime(LocalTime.MAX));
        BigDecimal totalAmount = orderRepository.sumGdnAmountBetweenDates(startDate.atStartOfDay(), now.atTime(LocalTime.MAX));
        totalOrders = totalOrders != null ? totalOrders : 0L;
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;

        // Get user stats
        DashboardResponse.UserStats userStats = getUserStats();

        DashboardResponse response = new DashboardResponse();
        response.setYearToDate(yearToDate);
        response.setMonthToDate(monthToDate);
        response.setWeekToDate(weekToDate);
        response.setSalesByRegion(regionSales);
        response.setSalesByCategory(categorySales);
        response.setTotalOrders(totalOrders);
        response.setTotalAmount(totalAmount);
        response.setUserStats(userStats);

        log.info("Exiting getDashboardData() with response for period: {}", period);
        return response;
    }

    private DashboardResponse.UserStats getUserStats() {
        log.debug("Entering getUserStats()");

        long users = userRepository.countAllUsers();
        long salespersons = salesPersonRepository.countActiveSalesPersons();
        long distributors = distributorRepository.countAllDistributors();
        long dealers = dealerRepository.countAllActiveDealers();
        long totalUsers = users + salespersons + distributors + dealers;

        DashboardResponse.UserStats stats = new DashboardResponse.UserStats();
        stats.setUsers(users);
        stats.setSalespersons(salespersons);
        stats.setDistributors(distributors);
        stats.setDealers(dealers);
        stats.setTotalUsers(totalUsers);

        log.debug("Exiting getUserStats() - users: {}, salespersons: {}, distributors: {}, dealers: {}, total: {}",
                users, salespersons, distributors, dealers, totalUsers);
        return stats;
    }

    private DashboardResponse.SalesMetrics getSalesMetrics(LocalDate start, LocalDate end) {
        log.debug("Entering getSalesMetrics() from {} to {}", start, end);

        BigDecimal totalSales = orderRepository.getTotalAmountBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX));
        Long transactionCount = orderRepository.countOrdersBetweenDates(start.atStartOfDay(), end.atTime(LocalTime.MAX));

        // Handle null values
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        transactionCount = transactionCount != null ? transactionCount : 0L;

        DashboardResponse.SalesMetrics metrics = new DashboardResponse.SalesMetrics(totalSales, transactionCount);
        log.debug("Exiting getSalesMetrics() with totalSales: {}, transactionCount: {}", totalSales, transactionCount);
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

    private Map<String, BigDecimal> getSalesByRegion(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting sales by region from {} to {}", startDate, endDate);

        // Get all orders in the date range and group by salesperson region
        List<com.nector.userservice.model.OrderWithSalesPerson> orders = orderRepository.findByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

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
                            return zone != null && !zone.trim().isEmpty() ? zone.toUpperCase() : "UNASSIGNED";
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
        BigDecimal totalAmount = orderRepository.getTotalAmountBetweenDates(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        categorySales.put("General", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        return categorySales;
    }

    @Transactional(readOnly = true)
    public MonthlySalesResponse getMonthlySalesData() {
        LocalDate today = LocalDate.now();
        // Indian FY: April 1 to March 31
        int fyStartYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        LocalDate fyStart = LocalDate.of(fyStartYear, 4, 1);
        LocalDate fyEnd = LocalDate.of(fyStartYear + 1, 3, 31);

        String financialYear = "FY " + fyStartYear + "-" + String.format("%02d", (fyStartYear + 1) % 100);

        List<Object[]> results = orderRepository.getMonthlyGdnRevenue(
                fyStart.atStartOfDay(), fyEnd.atTime(LocalTime.MAX));

        Map<String, MonthlySalesResponse.MonthlyData> dataMap = new HashMap<>();
        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            BigDecimal revenue = row[2] instanceof BigDecimal ? (BigDecimal) row[2] : new BigDecimal(row[2].toString());
            long orderCount = ((Number) row[3]).longValue();
            dataMap.put(year + "-" + month,
                    new MonthlySalesResponse.MonthlyData(monthAbbr(month), year, revenue, orderCount));
        }

        // All 12 months in FY order: Apr(4) … Dec(12), Jan(1) … Mar(3)
        int[] fyMonths = {4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3};
        List<MonthlySalesResponse.MonthlyData> months = new ArrayList<>();
        for (int m : fyMonths) {
            int yr = m >= 4 ? fyStartYear : fyStartYear + 1;
            months.add(dataMap.getOrDefault(yr + "-" + m,
                    new MonthlySalesResponse.MonthlyData(monthAbbr(m), yr, BigDecimal.ZERO, 0L)));
        }

        return new MonthlySalesResponse(financialYear, months);
    }

    private String monthAbbr(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> "Unknown";
        };
    }
}