package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.bom.repository.RawMaterialInventoryLotRepository;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;
import com.nector.userservice.interceptors.reports.service.StockMovementReportService;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.repository.OutwardItemTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementReportServiceImpl implements StockMovementReportService {

    private final RawMaterialInventoryLotRepository lotRepository;
    private final OutwardItemTransactionRepository outwardRepository;

    @Override
    public List<StockMovementRowDto> getLedger(ReportFilterRequest filter) {
        List<StockMovementRowDto> rows = new ArrayList<>();
        rows.addAll(getInwardSummary(filter));
        rows.addAll(getOutwardSummary(filter));
        rows.sort(Comparator.comparing(StockMovementRowDto::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    @Override
    public List<StockMovementRowDto> getInwardSummary(ReportFilterRequest filter) {
        Instant from = resolveFrom(filter).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = resolveTo(filter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        return lotRepository.findByReceivedAtBetween(from, to).stream().map(lot ->
            StockMovementRowDto.builder()
                .movementType("INWARD")
                .materialCode(String.valueOf(lot.getRawMaterialId()))
                .materialName("Raw Material ID: " + lot.getRawMaterialId())
                .quantity(lot.getQuantityOriginal())
                .pricePerUnit(lot.getPricePerUnit())
                .date(LocalDateTime.ofInstant(lot.getReceivedAt(), ZoneOffset.UTC))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<StockMovementRowDto> getOutwardSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return outwardRepository.findByDateRange(from, to, PageRequest.of(0, 500))
                .stream().map(o ->
            StockMovementRowDto.builder()
                .movementType("OUTWARD")
                .materialCode(o.getMaterialCode())
                .materialName(o.getMaterialName())
                .quantity(o.getQuantity() != null ? java.math.BigDecimal.valueOf(o.getQuantity()) : null)
                .unit(o.getUnit() != null ? o.getUnit().name() : null)
                .pricePerUnit(o.getQuotedSellingPrice())
                .reference(o.getReferenceNumber())
                .date(o.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
