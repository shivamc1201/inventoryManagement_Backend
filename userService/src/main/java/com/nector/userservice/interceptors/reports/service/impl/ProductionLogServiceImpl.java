package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;
import com.nector.userservice.interceptors.reports.entity.ProductionLog;
import com.nector.userservice.interceptors.reports.entity.ProductionLogComponent;
import com.nector.userservice.interceptors.reports.repository.ProductionLogRepository;
import com.nector.userservice.interceptors.reports.service.ProductionLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionLogServiceImpl implements ProductionLogService {

    private final ProductionLogRepository productionLogRepository;

    @Override
    @Transactional
    public ProductionLogDto create(ProductionLogDto dto) {
        ProductionLog log = toEntity(dto);
        log = productionLogRepository.save(log);
        return toDto(log);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionLogDto getById(Long id) {
        return toDto(productionLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductionLog not found: " + id)));
    }

    @Override
    @Transactional
    public ProductionLogDto update(Long id, ProductionLogDto dto) {
        ProductionLog existing = productionLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductionLog not found: " + id));
        existing.setFinishedProductName(dto.getFinishedProductName());
        existing.setBatchNumber(dto.getBatchNumber());
        existing.setQuantityProduced(dto.getQuantityProduced());
        existing.setProductionDate(dto.getProductionDate());
        existing.setShift(dto.getShift());
        existing.setOperatorName(dto.getOperatorName());
        existing.setSupervisorName(dto.getSupervisorName());
        existing.setTotalRawMaterialCost(dto.getTotalRawMaterialCost());
        existing.setTotalAdditionalCost(dto.getTotalAdditionalCost());
        existing.setTotalProductionCost(dto.getTotalProductionCost());
        existing.setCostPerUnit(dto.getCostPerUnit());
        existing.setStatus(dto.getStatus());
        existing.setRemarks(dto.getRemarks());
        return toDto(productionLogRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productionLogRepository.deleteById(id);
    }

    private ProductionLog toEntity(ProductionLogDto dto) {
        ProductionLog log = ProductionLog.builder()
                .productionNumber(dto.getProductionNumber())
                .bomId(dto.getBomId())
                .finishedProductId(dto.getFinishedProductId())
                .finishedProductName(dto.getFinishedProductName())
                .batchNumber(dto.getBatchNumber())
                .quantityProduced(dto.getQuantityProduced())
                .outputUnit(dto.getOutputUnit())
                .productionDate(dto.getProductionDate())
                .shift(dto.getShift())
                .operatorName(dto.getOperatorName())
                .supervisorName(dto.getSupervisorName())
                .totalRawMaterialCost(dto.getTotalRawMaterialCost())
                .totalAdditionalCost(dto.getTotalAdditionalCost())
                .totalProductionCost(dto.getTotalProductionCost())
                .costPerUnit(dto.getCostPerUnit())
                .status(dto.getStatus() != null ? dto.getStatus() : "IN_PROGRESS")
                .remarks(dto.getRemarks())
                .build();
        if (dto.getComponents() != null) {
            dto.getComponents().forEach(c -> {
                ProductionLogComponent comp = ProductionLogComponent.builder()
                        .rawMaterialId(c.getRawMaterialId())
                        .rawMaterialName(c.getRawMaterialName())
                        .quantityPlanned(c.getQuantityPlanned())
                        .quantityActual(c.getQuantityActual())
                        .unit(c.getUnit())
                        .rate(c.getRate())
                        .amount(c.getAmount())
                        .varianceQty(c.getVarianceQty())
                        .productionLog(log)
                        .build();
                log.getComponents().add(comp);
            });
        }
        return log;
    }

    private ProductionLogDto toDto(ProductionLog log) {
        List<ProductionLogDto.ProductionLogComponentDto> components = log.getComponents().stream()
                .map(c -> ProductionLogDto.ProductionLogComponentDto.builder()
                        .rawMaterialId(c.getRawMaterialId())
                        .rawMaterialName(c.getRawMaterialName())
                        .quantityPlanned(c.getQuantityPlanned())
                        .quantityActual(c.getQuantityActual())
                        .unit(c.getUnit())
                        .rate(c.getRate())
                        .amount(c.getAmount())
                        .varianceQty(c.getVarianceQty())
                        .build())
                .collect(Collectors.toList());
        return ProductionLogDto.builder()
                .id(log.getId())
                .productionNumber(log.getProductionNumber())
                .bomId(log.getBomId())
                .finishedProductId(log.getFinishedProductId())
                .finishedProductName(log.getFinishedProductName())
                .batchNumber(log.getBatchNumber())
                .quantityProduced(log.getQuantityProduced())
                .outputUnit(log.getOutputUnit())
                .productionDate(log.getProductionDate())
                .shift(log.getShift())
                .operatorName(log.getOperatorName())
                .supervisorName(log.getSupervisorName())
                .totalRawMaterialCost(log.getTotalRawMaterialCost())
                .totalAdditionalCost(log.getTotalAdditionalCost())
                .totalProductionCost(log.getTotalProductionCost())
                .costPerUnit(log.getCostPerUnit())
                .status(log.getStatus())
                .remarks(log.getRemarks())
                .createdAt(log.getCreatedAt())
                .components(components)
                .build();
    }
}
