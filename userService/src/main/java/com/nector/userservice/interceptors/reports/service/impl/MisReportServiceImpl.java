package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.MisKpiSummaryDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import com.nector.userservice.interceptors.reports.service.MisReportService;
import com.nector.userservice.model.Invoice;
import com.nector.userservice.repository.InvoiceRepository;
import com.nector.userservice.repository.PaymentApprovalRepository;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MisReportServiceImpl implements MisReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final GdnRepository gdnRepository;
    private final OrderTrackingRepository orderTrackingRepository;

    @Override
    public MisKpiSummaryDto getKpiSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);

        BigDecimal totalSales = invoiceRepository.sumGrandTotalByDistributorAndDateRange(
                filter.getDistributorId(), from, to);

        BigDecimal totalCollections = filter.getDistributorId() != null
                ? paymentApprovalRepository.sumApprovedPaymentsByDistributorAndDateRange(
                        filter.getDistributorId(), from, to)
                : BigDecimal.ZERO;

        long openOrders = orderTrackingRepository.countPending();
        List<Object[]> dispatchRows = gdnRepository.getDispatchSummary(from, to);
        long dispatchVol = (!dispatchRows.isEmpty() && dispatchRows.get(0)[0] != null)
                ? ((Number) dispatchRows.get(0)[0]).longValue()
                : 0L;

        List<Object[]> trendRows = invoiceRepository.getMonthlySalesTrend(filter.getDistributorId(), from, to);
        List<Map<String, Object>> trend = trendRows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("month", r[0]);
            m.put("year", r[1]);
            m.put("total", r[2]);
            m.put("count", r[3]);
            return m;
        }).collect(Collectors.toList());

        List<Object[]> rankRows = invoiceRepository.getSalesByDistributor(from, to, PageRequest.of(0, 10));
        List<Map<String, Object>> ranking = rankRows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("distributorId", r[0]);
            m.put("distributorName", r[1]);
            m.put("orderCount", r[2]);
            m.put("totalValue", r[3]);
            return m;
        }).collect(Collectors.toList());

        return MisKpiSummaryDto.builder()
                .totalSalesValue(totalSales != null ? totalSales : BigDecimal.ZERO)
                .totalCollections(totalCollections != null ? totalCollections : BigDecimal.ZERO)
                .openOrdersCount(openOrders)
                .dispatchVolume(dispatchVol)
                .monthlyTrend(trend)
                .distributorRanking(ranking)
                .build();
    }

    @Override
    public List<SalesInvoiceRowDto> getRecentInvoices(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceRepository.findByDistributorAndDateRange(
                filter.getDistributorId(), from, to, PageRequest.of(0, 10))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SalesInvoiceRowDto toDto(Invoice i) {
        return SalesInvoiceRowDto.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .distributorId(i.getDistributorId())
                .distributorName(i.getDistributorName())
                .invoiceDate(i.getInvoiceDate())
                .totalAmount(i.getTotalAmount())
                .taxAmount(i.getTaxAmount())
                .grandTotal(i.getGrandTotal())
                .invoiceStatus(i.getInvoiceStatus() != null ? i.getInvoiceStatus().name() : null)
                .gdnNumber(i.getGdnNumber())
                .build();
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        if (f.getStartDate() != null) return f.getStartDate();
        return LocalDate.now().withDayOfYear(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        if (f.getEndDate() != null) return f.getEndDate();
        return LocalDate.now();
    }
}
