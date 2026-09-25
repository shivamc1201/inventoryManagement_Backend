package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.dispatch.entity.Gdn;
import com.nector.userservice.dispatch.entity.Gdn.DeliveryStatus;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.interceptors.distributor.model.OrderConfirmation;
import com.nector.userservice.interceptors.distributor.model.OrderConfirmationRequest;
import com.nector.userservice.interceptors.distributor.repository.OrderConfirmationRepository;
import com.nector.userservice.interceptors.reports.dto.DeliveryConfirmationRowDto;
import com.nector.userservice.interceptors.reports.dto.DispatchRegisterRowDto;
import com.nector.userservice.interceptors.reports.dto.PendingDispatchRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.DispatchReportService;
import com.nector.userservice.model.Cart;
import com.nector.userservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchReportServiceImpl implements DispatchReportService {

    private final GdnRepository gdnRepository;
    private final CartRepository cartRepository;
    private final OrderConfirmationRepository orderConfirmationRepository;

    @Override
    public Page<DispatchRegisterRowDto> getRegister(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("gdnDate").descending());
        DeliveryStatus status = filter.getDeliveryStatus() != null
                ? DeliveryStatus.valueOf(filter.getDeliveryStatus().toUpperCase()) : null;
        Page<Gdn> gdns = gdnRepository.findByGdnDateBetween(from, to, status, page);

        Map<Long, String> customerNames = loadCustomerNames(gdns.getContent());
        return gdns.map(g -> toRegisterDto(g, customerNames.getOrDefault(g.getOrderId(), "")));
    }

    @Override
    public Map<String, Object> getSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        List<Object[]> rows = gdnRepository.getDispatchSummary(from, to);
        Map<String, Object> result = new HashMap<>();
        if (!rows.isEmpty()) {
            Object[] summary = rows.get(0);
            result.put("totalDispatches", summary[0]);
            result.put("totalWeight", summary[1]);
            result.put("totalPackages", summary[2]);
        }
        return result;
    }

    @Override
    public Page<PendingDispatchRowDto> getPendingDispatches(ReportFilterRequest filter) {
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize());
        Page<Gdn> gdns = gdnRepository.findByDeliveryStatusOrderByGdnDateAsc(Gdn.DeliveryStatus.PENDING, page);

        List<Long> orderIds = gdns.getContent().stream().map(Gdn::getOrderId).collect(Collectors.toList());
        Map<Long, Cart> cartMap = cartRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Cart::getId, c -> c));

        return gdns.map(g -> toPendingDto(g, cartMap.get(g.getOrderId())));
    }

    @Override
    public Page<DeliveryConfirmationRowDto> getDeliveryConfirmations(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("confirmedAt").descending());
        Page<OrderConfirmation> confirmations = orderConfirmationRepository.findConfirmationsForReport(from, to, page);

        List<Long> orderIds = confirmations.getContent().stream().map(OrderConfirmation::getOrderId).collect(Collectors.toList());
        Map<Long, String> customerNames = cartRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Cart::getId, c -> c.getDistributorName() != null ? c.getDistributorName() : ""));

        return confirmations.map(oc -> toDeliveryConfirmationDto(oc, customerNames.getOrDefault(oc.getOrderId(), "")));
    }

    private DispatchRegisterRowDto toRegisterDto(Gdn g, String customerName) {
        return DispatchRegisterRowDto.builder()
                .id(g.getId())
                .gdnNumber(g.getGdnNumber())
                .orderId(g.getOrderId())
                .gdnDate(g.getGdnDate())
                .vehicleNo(g.getVehicleNo())
                .transportName(g.getTransportName())
                .driverName(g.getDriverName())
                .driverMobile(g.getDriverMobile())
                .totalPackages(g.getTotalPackages())
                .totalWeight(g.getTotalWeight())
                .shippingAddress(g.getShippingAddress())
                .itemCount(g.getGdnItems() != null ? g.getGdnItems().size() : 0)
                .deliveryStatus(g.getDeliveryStatus() != null ? g.getDeliveryStatus().name() : DeliveryStatus.PENDING.name())
                .customerName(customerName)
                .build();
    }

    private PendingDispatchRowDto toPendingDto(Gdn g, Cart cart) {
        String customer = cart != null && cart.getDistributorName() != null ? cart.getDistributorName() : "";
        java.math.BigDecimal amount = cart != null ? cart.getTotalCartAmount() : java.math.BigDecimal.ZERO;
        long readySinceDays = g.getGdnDate() != null
                ? ChronoUnit.DAYS.between(g.getGdnDate().toLocalDate(), LocalDate.now())
                : 0;
        return PendingDispatchRowDto.builder()
                .id(g.getId())
                .challanNo(g.getGdnNumber())
                .customer(customer)
                .items(g.getGdnItems() != null ? g.getGdnItems().size() : 0)
                .amount(amount)
                .readySinceDays(readySinceDays)
                .gdnDate(g.getGdnDate())
                .build();
    }

    private DeliveryConfirmationRowDto toDeliveryConfirmationDto(OrderConfirmation oc, String customerName) {
        return DeliveryConfirmationRowDto.builder()
                .id(oc.getId())
                .challanNo(oc.getGdnNumber())
                .customer(customerName)
                .deliveredDate(oc.getConfirmedAt())
                .receivedBy(oc.getReceivedBy())
                .podStatus(mapPodStatus(oc.getStatus()))
                .build();
    }

    private String mapPodStatus(OrderConfirmationRequest.ConfirmationStatus status) {
        if (status == null) return "Unknown";
        return switch (status) {
            case RECEIVED_COMPLETE -> "Confirmed";
            case RECEIVED_PARTIAL -> "Partial";
            case DAMAGED -> "Damaged";
            case REJECTED -> "Rejected";
        };
    }

    private Map<Long, String> loadCustomerNames(List<Gdn> gdns) {
        List<Long> orderIds = gdns.stream().map(Gdn::getOrderId).collect(Collectors.toList());
        return cartRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Cart::getId, c -> c.getDistributorName() != null ? c.getDistributorName() : ""));
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
