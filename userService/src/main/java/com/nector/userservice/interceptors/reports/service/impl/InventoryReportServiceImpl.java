package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.InventorySnapshotDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.InventoryReportService;
import com.nector.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryReportServiceImpl implements InventoryReportService {

    private final FinishedProductRepository finishedProductRepository;
    private final RawProductRepository rawProductRepository;
    private final ScrapItemRepository scrapItemRepository;

    @Override
    public List<InventorySnapshotDto> getSnapshot(String category) {
        List<InventorySnapshotDto> result = new ArrayList<>();
        if (category == null || "FINISHED".equalsIgnoreCase(category)) {
            finishedProductRepository.findByActiveTrue().forEach(fp ->
                result.add(InventorySnapshotDto.builder()
                    .id(fp.getId()).category("FINISHED").itemName(fp.getName()).sku(fp.getSku())
                    .quantity(fp.getQuantity()).price(fp.getPrice())
                    .totalValue(fp.getPrice() != null && fp.getQuantity() != null
                        ? fp.getPrice().multiply(BigDecimal.valueOf(fp.getQuantity())) : BigDecimal.ZERO)
                    .minimumThreshold(fp.getMinimumThreshold())
                    .belowThreshold(fp.getMinimumThreshold() != null && fp.getQuantity() != null
                        && fp.getQuantity() <= fp.getMinimumThreshold())
                    .build()));
        }
        if (category == null || "RAW".equalsIgnoreCase(category)) {
            rawProductRepository.findByActiveTrue().forEach(rp -> {
                int qty = rp.getQuantity() != null ? rp.getQuantity().intValue() : 0;
                Integer threshold = rp.getMinimumThreshold() != null ? rp.getMinimumThreshold().intValue() : null;
                result.add(InventorySnapshotDto.builder()
                    .id(rp.getId()).category("RAW").itemName(rp.getName()).sku(rp.getMaterialCode())
                    .quantity(qty).price(rp.getPrice())
                    .totalValue(rp.getPrice() != null ? rp.getPrice().multiply(rp.getQuantity()) : BigDecimal.ZERO)
                    .minimumThreshold(threshold)
                    .belowThreshold(threshold != null && qty <= threshold)
                    .build());
            });
        }
        if (category == null || "SCRAP".equalsIgnoreCase(category)) {
            scrapItemRepository.findByActiveTrue().forEach(si ->
                result.add(InventorySnapshotDto.builder()
                    .id(si.getId()).category("SCRAP").itemName(si.getName()).sku(si.getItemCode())
                    .quantity(si.getQuantity()).price(si.getPrice())
                    .totalValue(si.getPrice() != null && si.getQuantity() != null
                        ? si.getPrice().multiply(BigDecimal.valueOf(si.getQuantity())) : BigDecimal.ZERO)
                    .minimumThreshold(si.getMinimumThreshold())
                    .belowThreshold(si.getMinimumThreshold() != null && si.getQuantity() != null
                        && si.getQuantity() <= si.getMinimumThreshold())
                    .build()));
        }
        return result;
    }

    @Override
    public List<InventorySnapshotDto> getLowStock(String category) {
        List<InventorySnapshotDto> result = new ArrayList<>();
        if (category == null || "FINISHED".equalsIgnoreCase(category)) {
            finishedProductRepository.findLowStockItems().forEach(fp ->
                result.add(InventorySnapshotDto.builder()
                    .id(fp.getId()).category("FINISHED").itemName(fp.getName()).sku(fp.getSku())
                    .quantity(fp.getQuantity()).price(fp.getPrice())
                    .minimumThreshold(fp.getMinimumThreshold()).belowThreshold(true).build()));
        }
        if (category == null || "RAW".equalsIgnoreCase(category)) {
            rawProductRepository.findLowStockItems().forEach(rp -> {
                int qty = rp.getQuantity() != null ? rp.getQuantity().intValue() : 0;
                Integer threshold = rp.getMinimumThreshold() != null ? rp.getMinimumThreshold().intValue() : null;
                result.add(InventorySnapshotDto.builder()
                    .id(rp.getId()).category("RAW").itemName(rp.getName()).sku(rp.getMaterialCode())
                    .quantity(qty).price(rp.getPrice())
                    .minimumThreshold(threshold).belowThreshold(true).build());
            });
        }
        if (category == null || "SCRAP".equalsIgnoreCase(category)) {
            scrapItemRepository.findLowStockItems().forEach(si ->
                result.add(InventorySnapshotDto.builder()
                    .id(si.getId()).category("SCRAP").itemName(si.getName()).sku(si.getItemCode())
                    .quantity(si.getQuantity()).price(si.getPrice())
                    .minimumThreshold(si.getMinimumThreshold()).belowThreshold(true).build()));
        }
        return result;
    }
}
