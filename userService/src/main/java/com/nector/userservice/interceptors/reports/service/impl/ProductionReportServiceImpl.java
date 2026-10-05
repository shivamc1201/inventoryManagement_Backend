package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.bom.repository.BillOfMaterialRepository;
import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.repository.ProductionLogRepository;
import com.nector.userservice.interceptors.reports.service.ProductionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionReportServiceImpl implements ProductionReportService {

    private final ProductionLogRepository productionLogRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;

    @Override
    public Page<ProductionLogDto> getProductionLog(ReportFilterRequest filter) {
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("productionDate").descending());
        return productionLogRepository.findByProductionDateBetweenOrderByProductionDateDesc(from, to, page)
                .map(log -> ProductionLogDto.builder()
                        .id(log.getId())
                        .productionNumber(log.getProductionNumber())
                        .finishedProductId(log.getFinishedProductId())
                        .finishedProductName(log.getFinishedProductName())
                        .batchNumber(log.getBatchNumber())
                        .quantityProduced(log.getQuantityProduced())
                        .outputUnit(log.getOutputUnit())
                        .productionDate(log.getProductionDate())
                        .shift(log.getShift())
                        .totalProductionCost(log.getTotalProductionCost())
                        .costPerUnit(log.getCostPerUnit())
                        .status(log.getStatus())
                        .createdAt(log.getCreatedAt())
                        .build());
    }

    @Override
    public List<Map<String, Object>> getProductionSummary(ReportFilterRequest filter) {
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        return productionLogRepository.getProductionSummaryByProduct(from, to).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("finishedProductId", r[0]);
            m.put("finishedProductName", r[1]);
            m.put("runCount", r[2]);
            m.put("totalQuantity", r[3]);
            m.put("totalCost", r[4]);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBomConsumption(ReportFilterRequest filter) {
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        productionLogRepository.findByProductionDateBetweenOrderByProductionDateDesc(from, to, PageRequest.of(0, 200))
                .forEach(log -> log.getComponents().forEach(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productionNumber", log.getProductionNumber());
                    m.put("productName", log.getFinishedProductName());
                    m.put("rawMaterialName", c.getRawMaterialName());
                    m.put("quantityPlanned", c.getQuantityPlanned());
                    m.put("quantityActual", c.getQuantityActual());
                    m.put("variance", c.getVarianceQty());
                    result.add(m);
                }));
        return result;
    }

    @Override
    public List<Map<String, Object>> getBomReport() {
        return billOfMaterialRepository.findAll().stream().map(bom -> {
            Map<String, Object> m = new HashMap<>();
            m.put("bomId", bom.getId());
            m.put("bomName", bom.getBomName());
            m.put("finishedProductId", bom.getFinishedProductId());
            m.put("finishedProductName", bom.getFinishedProductName());
            m.put("outputQuantity", bom.getOutputQuantity());
            m.put("outputUnit", bom.getOutputUnit());
            m.put("totalComponentCost", bom.getTotalComponentCost());
            m.put("totalAdditionalCost", bom.getTotalAdditionalCost());
            m.put("effectiveCost", bom.getEffectiveCost());
            m.put("effectiveRatePerUnit", bom.getEffectiveRatePerUnit());
            m.put("components", bom.getComponents().stream().map(c -> {
                Map<String, Object> cm = new HashMap<>();
                cm.put("rawMaterialId", c.getRawMaterialId());
                cm.put("rawMaterialName", c.getRawMaterialName());
                cm.put("quantity", c.getQuantity());
                cm.put("unit", c.getUnit());
                cm.put("rate", c.getRate());
                cm.put("amount", c.getAmount());
                cm.put("contributionPercent", c.getContributionPercent());
                return cm;
            }).collect(Collectors.toList()));
            m.put("additionalCosts", bom.getAdditionalCosts().stream().map(ac -> {
                Map<String, Object> acm = new HashMap<>();
                acm.put("type", ac.getType());
                acm.put("percentage", ac.getPercentage());
                acm.put("amount", ac.getAmount());
                return acm;
            }).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getProductionCostReport(ReportFilterRequest filter) {
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        return productionLogRepository.findByProductionDateBetweenOrderByProductionDateDesc(from, to).stream()
                .map(log -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productionNumber", log.getProductionNumber());
                    m.put("finishedProductId", log.getFinishedProductId());
                    m.put("finishedProductName", log.getFinishedProductName());
                    m.put("batchNumber", log.getBatchNumber());
                    m.put("productionDate", log.getProductionDate());
                    m.put("quantityProduced", log.getQuantityProduced());
                    m.put("outputUnit", log.getOutputUnit());
                    m.put("totalRawMaterialCost", log.getTotalRawMaterialCost());
                    m.put("totalAdditionalCost", log.getTotalAdditionalCost());
                    m.put("totalProductionCost", log.getTotalProductionCost());
                    m.put("costPerUnit", log.getCostPerUnit());
                    m.put("status", log.getStatus());
                    return m;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDailySummary(ReportFilterRequest filter) {
        LocalDate from = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(1);
        LocalDate to = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        return productionLogRepository.getDailySummary(from, to).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productionDate", r[0]);
            m.put("totalRuns", r[1]);
            m.put("totalQuantityProduced", r[2]);
            m.put("totalProductionCost", r[3]);
            return m;
        }).collect(Collectors.toList());
    }
}
