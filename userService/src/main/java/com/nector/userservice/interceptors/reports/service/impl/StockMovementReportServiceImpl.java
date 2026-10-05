package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.bom.entity.RawMaterialInventoryLot;
import com.nector.userservice.bom.repository.RawMaterialInventoryLotRepository;
import com.nector.userservice.interceptors.reports.dto.MaterialInwardRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.StockMovementRowDto;
import com.nector.userservice.interceptors.reports.dto.SupplierInwardSummaryDto;
import com.nector.userservice.interceptors.reports.service.StockMovementReportService;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.model.RawProduct;
import com.nector.userservice.repository.OutwardItemTransactionRepository;
import com.nector.userservice.repository.RawProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementReportServiceImpl implements StockMovementReportService {

    private final RawMaterialInventoryLotRepository lotRepository;
    private final OutwardItemTransactionRepository outwardRepository;
    private final RawProductRepository rawProductRepository;

    @Override
    public List<StockMovementRowDto> getLedger(ReportFilterRequest filter) {
        List<StockMovementRowDto> rows = new ArrayList<>();
        rows.addAll(getInwardSummary(filter));
        rows.addAll(getOutwardSummary(filter));
        rows.sort(Comparator.comparing(StockMovementRowDto::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal running = BigDecimal.ZERO;
        for (StockMovementRowDto row : rows) {
            if (row.getInwardQty() != null) {
                running = running.add(row.getInwardQty());
            } else if (row.getOutwardQty() != null) {
                running = running.subtract(row.getOutwardQty());
            }
            row.setBalanceQty(running);
        }

        rows.sort(Comparator.comparing(StockMovementRowDto::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    private List<StockMovementRowDto> getInwardSummary(ReportFilterRequest filter) {
        Instant from = resolveFrom(filter).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = resolveTo(filter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        List<RawMaterialInventoryLot> lots = lotRepository.findByReceivedAtBetween(from, to, filter.getSupplierId());

        Map<Long, String> nameById = buildRawProductNameMap(lots.stream()
                .map(RawMaterialInventoryLot::getRawMaterialId).collect(Collectors.toSet()));

        return lots.stream().map(lot -> StockMovementRowDto.builder()
                .movementType("INWARD")
                .materialCode(String.valueOf(lot.getRawMaterialId()))
                .materialName(nameById.getOrDefault(lot.getRawMaterialId(),
                        "Raw Material #" + lot.getRawMaterialId()))
                .quantity(lot.getQuantityOriginal())
                .inwardQty(lot.getQuantityOriginal())
                .pricePerUnit(lot.getPricePerUnit())
                .date(LocalDateTime.ofInstant(lot.getReceivedAt(), ZoneOffset.UTC))
                .supplierId(lot.getSupplierId())
                .supplierName(lot.getSupplierName())
                .partyName(lot.getSupplierName())
                .voucherNo(lot.getVoucherNo())
                .build()
        ).collect(Collectors.toList());
    }

    private List<StockMovementRowDto> getOutwardSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return outwardRepository.findByDateRange(from, to, PageRequest.of(0, 500))
                .stream().map(o -> StockMovementRowDto.builder()
                        .movementType("OUTWARD")
                        .materialCode(o.getMaterialCode())
                        .materialName(o.getMaterialName())
                        .quantity(o.getQuantity() != null ? BigDecimal.valueOf(o.getQuantity()) : null)
                        .outwardQty(o.getQuantity() != null ? BigDecimal.valueOf(o.getQuantity()) : null)
                        .unit(o.getUnit() != null ? o.getUnit().name() : null)
                        .pricePerUnit(o.getQuotedSellingPrice())
                        .reference(o.getReferenceNumber())
                        .voucherNo(o.getReferenceNumber())
                        .partyName(o.getIssuedTo())
                        .date(o.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public List<MaterialInwardRowDto> getMaterialInward(ReportFilterRequest filter) {
        Instant from = resolveFrom(filter).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = resolveTo(filter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        List<RawMaterialInventoryLot> lots = lotRepository.findByReceivedAtBetween(from, to, filter.getSupplierId());

        Map<Long, String> nameById = buildRawProductNameMap(lots.stream()
                .map(RawMaterialInventoryLot::getRawMaterialId).collect(Collectors.toSet()));

        return lots.stream().map(lot -> MaterialInwardRowDto.builder()
                .date(LocalDateTime.ofInstant(lot.getReceivedAt(), ZoneOffset.UTC))
                .itemName(nameById.getOrDefault(lot.getRawMaterialId(),
                        "Raw Material #" + lot.getRawMaterialId()))
                .batchNo(lot.getBatchNo())
                .supplierName(lot.getSupplierName())
                .qtyReceived(lot.getQuantityOriginal())
                .voucherNo(lot.getVoucherNo())
                .warehouseLocation(lot.getWarehouseLocation())
                .rawMaterialId(lot.getRawMaterialId())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<SupplierInwardSummaryDto> getSupplierWiseSummary(ReportFilterRequest filter) {
        Instant from = resolveFrom(filter).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = resolveTo(filter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<Object[]> summaryRows = lotRepository.getSupplierWiseSummary(from, to);
        List<Object[]> topItemRows = lotRepository.getTopItemBySupplier(from, to);

        // First rawMaterialId per supplierId (highest qty is first due to ORDER BY)
        Map<String, Long> topRawMaterialIdBySupplier = new LinkedHashMap<>();
        for (Object[] r : topItemRows) {
            String sid = (String) r[0];
            topRawMaterialIdBySupplier.putIfAbsent(sid, ((Number) r[1]).longValue());
        }

        Set<Long> rawMatIds = new HashSet<>(topRawMaterialIdBySupplier.values());
        Map<Long, String> nameById = buildRawProductNameMap(rawMatIds);

        return summaryRows.stream().map(r -> {
            String sid = (String) r[0];
            String sName = (String) r[1];
            long count = ((Number) r[2]).longValue();
            BigDecimal totalQty = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
            Instant lastInstant = (Instant) r[4];
            LocalDateTime lastReceipt = lastInstant != null
                    ? LocalDateTime.ofInstant(lastInstant, ZoneOffset.UTC) : null;

            Long topMatId = topRawMaterialIdBySupplier.get(sid);
            String topItem = topMatId != null
                    ? nameById.getOrDefault(topMatId, "Raw Material #" + topMatId) : null;

            return SupplierInwardSummaryDto.builder()
                    .supplierId(sid)
                    .supplierName(sName)
                    .receiptsCount(count)
                    .totalQtyReceived(totalQty)
                    .lastReceiptDate(lastReceipt)
                    .topItem(topItem)
                    .build();
        }).collect(Collectors.toList());
    }

    private Map<Long, String> buildRawProductNameMap(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return rawProductRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(RawProduct::getId, RawProduct::getName));
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
