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
import java.util.Objects;
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
        Instant now = Instant.now();
        LocalDateTime nowLocal = LocalDateTime.now();

        Map<String, BigDecimal> totalInward = new HashMap<>();
        Map<String, BigDecimal> totalOutward = new HashMap<>();

        lotRepository.sumInwardByMaterialBefore(now).forEach(row -> {
            String code = String.valueOf(((Number) row[0]).longValue());
            BigDecimal qty = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            totalInward.merge(code, qty, BigDecimal::add);
        });

        outwardRepository.sumOutwardByMaterialBefore(nowLocal).forEach(row -> {
            String code = (String) row[0];
            if (code == null) return;
            BigDecimal qty = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            if (totalInward.containsKey(code)) {
                totalOutward.merge(code, qty, BigDecimal::add);
            }
        });

        Set<Long> ids = totalInward.keySet().stream()
                .map(code -> { try { return Long.parseLong(code); } catch (NumberFormatException e) { return null; } })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nameById = buildRawProductNameMap(ids);

        return totalInward.entrySet().stream()
                .map(e -> {
                    Long id = Long.parseLong(e.getKey());
                    String name = nameById.get(id);
                    if (name == null) name = "Raw Material #" + e.getKey();
                    BigDecimal inward = e.getValue();
                    BigDecimal outward = totalOutward.getOrDefault(e.getKey(), BigDecimal.ZERO);
                    BigDecimal balance = inward.subtract(outward);
                    return StockMovementRowDto.builder()
                            .movementType("BALANCE")
                            .materialCode(e.getKey())
                            .materialName(name)
                            .inwardQty(inward)
                            .outwardQty(outward)
                            .balanceQty(balance)
                            .build();
                })
                .sorted(Comparator.comparing(StockMovementRowDto::getMaterialName,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> buildOpeningBalance(LocalDate startDate) {
        Instant beforeInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        LocalDateTime beforeLocal = startDate.atStartOfDay();

        Map<String, BigDecimal> balance = new HashMap<>();

        // Inward opening: raw material lots received before start date
        lotRepository.sumInwardByMaterialBefore(beforeInstant).forEach(row -> {
            String code = String.valueOf(((Number) row[0]).longValue());
            BigDecimal qty = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            balance.merge(code, qty, BigDecimal::add);
        });

        // Outward opening: only subtract for materials that have inward history.
        // Outward-only items (promotional) have no stock baseline, so their period balance starts from 0.
        outwardRepository.sumOutwardByMaterialBefore(beforeLocal).forEach(row -> {
            String code = (String) row[0];
            BigDecimal qty = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            if (balance.containsKey(code)) {
                balance.merge(code, qty.negate(), BigDecimal::add);
            }
        });

        return balance;
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

        // First rawMaterialId per supplierName (supplierId may be empty/null in data)
        Map<String, Long> topRawMaterialIdBySupplier = new LinkedHashMap<>();
        for (Object[] r : topItemRows) {
            String sName = (String) r[0];
            if (sName == null) continue;
            topRawMaterialIdBySupplier.putIfAbsent(sName, ((Number) r[1]).longValue());
        }

        Set<Long> rawMatIds = new HashSet<>(topRawMaterialIdBySupplier.values());
        Map<Long, String> nameById = buildRawProductNameMap(rawMatIds);

        return summaryRows.stream().map(r -> {
            String sid = (String) r[0];
            String sName = (String) r[1];
            long count = ((Number) r[2]).longValue();
            BigDecimal totalQty = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
            Instant lastInstant = toInstant(r[4]);
            LocalDateTime lastReceipt = lastInstant != null
                    ? LocalDateTime.ofInstant(lastInstant, ZoneOffset.UTC) : null;

            Long topMatId = topRawMaterialIdBySupplier.get(sName);
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
                .collect(Collectors.toMap(RawProduct::getId,
                        p -> p.getName() != null ? p.getName() : "Raw Material #" + p.getId()));
    }

    private Instant toInstant(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Instant) return (Instant) raw;
        if (raw instanceof java.sql.Timestamp) return ((java.sql.Timestamp) raw).toInstant();
        return null;
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
