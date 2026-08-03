package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesOrderRowDto;
import com.nector.userservice.interceptors.reports.service.SalesOrdersReportService;
import com.nector.userservice.ordertracking.entity.OrderTracking;
import com.nector.userservice.ordertracking.entity.StepStatus;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesOrdersReportServiceImpl implements SalesOrdersReportService {

    private final OrderTrackingRepository orderTrackingRepository;

    @Override
    public Page<SalesOrderRowDto> getOrderGrid(ReportFilterRequest filter) {
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("orderDate").descending());
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(3);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        Page<OrderTracking> orders = filter.getDistributorId() != null
                ? orderTrackingRepository.findByDistributorIdAndDateRange(filter.getDistributorId(), from, to, page)
                : orderTrackingRepository.findAllByDateRange(from, to, page);

        return orders.map(o -> {
            long completedSteps = o.getSteps().stream()
                    .filter(s -> s.getStatus() == StepStatus.COMPLETED).count();
            String status = computeStatus(o);
            return SalesOrderRowDto.builder()
                    .id(o.getId())
                    .orderNumber(o.getOrderNumber())
                    .distributorId(o.getDistributorId())
                    .distributorName(o.getDistributorName())
                    .orderDate(o.getOrderDate())
                    .totalAmount(o.getTotalAmount())
                    .currentStatus(status)
                    .build();
        });
    }

    @Override
    public Map<String, Object> getStatusCounts(Long distributorId) {
        long completed = orderTrackingRepository.countCompleted();
        long pending = orderTrackingRepository.countPending();
        long total = distributorId != null
                ? orderTrackingRepository.countByDistributorId(distributorId)
                : orderTrackingRepository.count();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("pending", pending);
        return result;
    }

    private String computeStatus(OrderTracking o) {
        boolean anyPending = o.getSteps().stream()
                .anyMatch(s -> s.getStatus() == StepStatus.PENDING || s.getStatus() == StepStatus.IN_PROGRESS);
        boolean allCompleted = o.getSteps().stream()
                .allMatch(s -> s.getStatus() == StepStatus.COMPLETED);
        if (allCompleted) return "COMPLETED";
        if (anyPending) return "IN_PROGRESS";
        return "PENDING";
    }
}
