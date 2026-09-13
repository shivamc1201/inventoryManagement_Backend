package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.ProductSalesRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import com.nector.userservice.interceptors.reports.service.SalesReportService;
import com.nector.userservice.model.Invoice;
import com.nector.userservice.repository.InvoiceLineItemRepository;
import com.nector.userservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesReportServiceImpl implements SalesReportService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;

    @Override
    public Page<SalesInvoiceRowDto> getInvoiceGrid(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("invoiceDate").descending());
        return invoiceRepository.findByDistributorAndDateRange(filter.getDistributorId(), from, to, page)
                .map(this::toDto);
    }

    @Override
    public List<Map<String, Object>> getByDistributor(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceRepository.getSalesByDistributor(from, to, PageRequest.of(0, 50)).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("distributorId", r[0]);
            m.put("distributorName", r[1]);
            m.put("invoiceCount", r[2]);
            m.put("totalValue", r[3]);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceRepository.getMonthlySalesTrend(filter.getDistributorId(), from, to).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("month", r[0]);
            m.put("year", r[1]);
            m.put("totalValue", r[2]);
            m.put("invoiceCount", r[3]);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductSalesRowDto> getByProduct(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceLineItemRepository.getProductSalesSummary(from, to).stream().map(r ->
            ProductSalesRowDto.builder()
                .productId(r[0] != null ? ((Number) r[0]).longValue() : null)
                .productName(r[1] != null ? r[1].toString() : null)
                .totalQuantity(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                .totalAmount(r[3] != null ? new java.math.BigDecimal(r[3].toString()) : java.math.BigDecimal.ZERO)
                .invoiceCount(r[4] != null ? ((Number) r[4]).longValue() : 0L)
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<ProductSalesRowDto> getTopProducts(ReportFilterRequest filter, int limit) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceLineItemRepository.getTopProductsByQuantity(from, to).stream()
            .limit(limit)
            .map(r -> ProductSalesRowDto.builder()
                .productId(r[0] != null ? ((Number) r[0]).longValue() : null)
                .productName(r[1] != null ? r[1].toString() : null)
                .totalQuantity(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                .totalAmount(r[3] != null ? new java.math.BigDecimal(r[3].toString()) : java.math.BigDecimal.ZERO)
                .build()
            ).collect(Collectors.toList());
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
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().withDayOfYear(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
