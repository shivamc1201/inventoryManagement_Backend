package com.nector.userservice.bom.service.impl;

import com.nector.userservice.bom.dto.*;
import com.nector.userservice.bom.entity.*;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.bom.mapper.BomMapper;
import com.nector.userservice.bom.repository.BillOfMaterialRepository;
import com.nector.userservice.bom.service.BomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BomServiceImpl implements BomService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final BomMapper bomMapper;

    @Override
    public BomResponseDto create(BomRequestDto requestDto) {
        log.info("Creating new BOM: {}", requestDto.getBomName());
        
        if (billOfMaterialRepository.existsByBomNameIgnoreCase(requestDto.getBomName())) {
            throw new IllegalArgumentException("BOM with name '" + requestDto.getBomName() + "' already exists");
        }

        BillOfMaterial bom = bomMapper.toEntity(requestDto);
        bomMapper.updateComponentsFromDto(requestDto.getComponents(), bom);
        bomMapper.updateAdditionalCostsFromDto(requestDto.getAdditionalCosts(), bom);
        computeCosts(bom);
        
        BillOfMaterial savedBom = billOfMaterialRepository.save(bom);
        log.info("Successfully created BOM with ID: {}", savedBom.getId());
        
        return bomMapper.toResponseDto(savedBom);
    }

    @Override
    public BomResponseDto update(Long id, BomRequestDto requestDto) {
        log.info("Updating BOM with ID: {}", id);
        
        BillOfMaterial existingBom = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found with ID: " + id));

        if (!existingBom.getBomName().equalsIgnoreCase(requestDto.getBomName()) &&
            billOfMaterialRepository.existsByBomNameIgnoreCase(requestDto.getBomName())) {
            throw new IllegalArgumentException("BOM with name '" + requestDto.getBomName() + "' already exists");
        }

        bomMapper.updateEntityFromDto(requestDto, existingBom);
        bomMapper.updateComponentsFromDto(requestDto.getComponents(), existingBom);
        bomMapper.updateAdditionalCostsFromDto(requestDto.getAdditionalCosts(), existingBom);
        computeCosts(existingBom);
        
        BillOfMaterial updatedBom = billOfMaterialRepository.save(existingBom);
        log.info("Successfully updated BOM with ID: {}", updatedBom.getId());
        
        return bomMapper.toResponseDto(updatedBom);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BomResponseDto> getAll(Pageable pageable) {
        log.debug("Fetching all BOMs with pagination");
        return billOfMaterialRepository.findAll(pageable)
                .map(bomMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BomResponseDto getById(Long id) {
        log.debug("Fetching BOM with ID: {}", id);
        BillOfMaterial bom = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found with ID: " + id));
        return bomMapper.toResponseDto(bom);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting BOM with ID: {}", id);
        
        if (!billOfMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("BOM not found with ID: " + id);
        }
        
        billOfMaterialRepository.deleteById(id);
        log.info("Successfully deleted BOM with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BomResponseDto> search(String keyword, Pageable pageable) {
        log.debug("Searching BOMs with keyword: {}", keyword);
        return billOfMaterialRepository.findByBomNameContainingIgnoreCaseOrFinishedProductNameContainingIgnoreCase(
                keyword, keyword, pageable)
                .map(bomMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BomSummaryDto getSummary() {
        log.debug("Generating BOM summary statistics");
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<BillOfMaterial> allBoms = billOfMaterialRepository.findAll(pageable);
        
        if (allBoms.isEmpty()) {
            return BomSummaryDto.builder()
                    .totalBoms(0L)
                    .totalMaterials(0L)
                    .averageRatePerUnit(BigDecimal.ZERO)
                    .build();
        }

        long totalBoms = allBoms.getTotalElements();
        long totalMaterials = allBoms.stream()
                .mapToLong(bom -> bom.getMaterialCount() != null ? bom.getMaterialCount() : 0)
                .sum();
        
        BigDecimal averageRatePerUnit = allBoms.stream()
                .filter(bom -> bom.getEffectiveRatePerUnit() != null)
                .map(BillOfMaterial::getEffectiveRatePerUnit)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allBoms.getContent().size()), 2, RoundingMode.HALF_UP);

        return BomSummaryDto.builder()
                .totalBoms(totalBoms)
                .totalMaterials(totalMaterials)
                .averageRatePerUnit(averageRatePerUnit)
                .build();
    }

    private void computeCosts(BillOfMaterial bom) {
        log.debug("Computing costs for BOM: {}", bom.getBomName());
        
        if (bom.getComponents() == null || bom.getComponents().isEmpty()) {
            throw new IllegalArgumentException("BOM must have at least one component");
        }

        BigDecimal totalComponentCost = BigDecimal.ZERO;

        for (BomComponent component : bom.getComponents()) {
            BigDecimal amount = component.getQuantity()
                    .multiply(component.getRate())
                    .setScale(2, RoundingMode.HALF_UP);
            component.setAmount(amount);
            totalComponentCost = totalComponentCost.add(amount);
        }

        bom.setTotalComponentCost(totalComponentCost);

        for (BomComponent component : bom.getComponents()) {
            if (totalComponentCost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal contributionPercent = component.getAmount()
                        .divide(totalComponentCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                component.setContributionPercent(contributionPercent);
            } else {
                component.setContributionPercent(BigDecimal.ZERO);
            }
        }

        bom.getComponents().sort(Comparator.comparing(BomComponent::getAmount).reversed());

        BigDecimal totalAdditionalCost = BigDecimal.ZERO;
        if (bom.getAdditionalCosts() != null) {
            for (AdditionalCost additionalCost : bom.getAdditionalCosts()) {
                if (additionalCost.getPercentage() != null) {
                    BigDecimal amount = totalComponentCost
                            .multiply(additionalCost.getPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    additionalCost.setAmount(amount);
                    totalAdditionalCost = totalAdditionalCost.add(amount);
                }
            }
        }

        bom.setTotalAdditionalCost(totalAdditionalCost);

        BigDecimal effectiveCost = totalComponentCost.add(totalAdditionalCost);
        bom.setEffectiveCost(effectiveCost);

        if (bom.getOutputQuantity() != null && bom.getOutputQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal effectiveRatePerUnit = effectiveCost
                    .divide(bom.getOutputQuantity(), 2, RoundingMode.HALF_UP);
            bom.setEffectiveRatePerUnit(effectiveRatePerUnit);
        } else {
            bom.setEffectiveRatePerUnit(BigDecimal.ZERO);
        }

        bom.setMaterialCount(bom.getComponents().size());

        log.debug("Cost computation completed for BOM: {}", bom.getBomName());
    }
}
