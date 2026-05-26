package com.nector.userservice.bom.service.impl;

import com.nector.userservice.bom.dto.*;
import com.nector.userservice.bom.entity.*;
import com.nector.userservice.bom.repository.RawMaterialInventoryLotRepository;
import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.RawProductNotFoundException;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.bom.mapper.BomMapper;
import com.nector.userservice.bom.repository.BillOfMaterialRepository;
import com.nector.userservice.bom.service.BomService;
import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.model.RawProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.RawProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
    private final RawProductRepository rawProductRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final RawMaterialInventoryLotRepository inventoryLotRepository;

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
            BigDecimal fifoRate = computeFifoRate(component.getRawMaterialId(), component.getQuantity());
            component.setRate(fifoRate);
            BigDecimal amount = component.getQuantity()
                    .multiply(fifoRate)
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

    /**
     * FIFO weighted-average rate for the given raw material and required quantity.
     * Consumes oldest lots first; falls back to the product's current price for any
     * quantity not covered by existing lots.
     */
    private BigDecimal computeFifoRate(Long rawMaterialId, BigDecimal requiredQty) {
        if (requiredQty == null || requiredQty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        List<RawMaterialInventoryLot> lots = inventoryLotRepository
                .findByRawMaterialIdAndQuantityRemainingGreaterThanOrderByReceivedAtAsc(
                        rawMaterialId, BigDecimal.ZERO);

        BigDecimal remaining = requiredQty;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (RawMaterialInventoryLot lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal consume = remaining.min(lot.getQuantityRemaining());
            totalCost = totalCost.add(consume.multiply(lot.getPricePerUnit()));
            remaining = remaining.subtract(consume);
        }

        // Fall back to current product price for any quantity not yet covered by lots
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fallback = rawProductRepository.findActiveById(rawMaterialId)
                    .map(RawProduct::getPrice)
                    .orElse(BigDecimal.ZERO);
            if (fallback == null) fallback = BigDecimal.ZERO;
            totalCost = totalCost.add(remaining.multiply(fallback));
        }

        return totalCost.divide(requiredQty, 2, RoundingMode.HALF_UP);
    }

    /** Deducts the given quantity from FIFO lots (oldest first). */
    private void deductFromFifoLots(Long rawMaterialId, BigDecimal qty) {
        List<RawMaterialInventoryLot> lots = inventoryLotRepository
                .findByRawMaterialIdAndQuantityRemainingGreaterThanOrderByReceivedAtAsc(
                        rawMaterialId, BigDecimal.ZERO);

        BigDecimal remaining = qty;
        for (RawMaterialInventoryLot lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal consume = remaining.min(lot.getQuantityRemaining());
            lot.setQuantityRemaining(lot.getQuantityRemaining().subtract(consume));
            inventoryLotRepository.save(lot);
            remaining = remaining.subtract(consume);
        }
    }

    @Override
    public void recomputeBomCosts(Long bomId) {
        BillOfMaterial bom = billOfMaterialRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found with ID: " + bomId));
        computeCosts(bom);
        billOfMaterialRepository.save(bom);
        log.info("Recomputed costs for BOM ID: {}", bomId);
    }

    @Override
    public BomProductionResponseDto produceFinishedProduct(BomProductionRequestDto requestDto) {
        log.info("Producing finished product: {} with quantity: {}",
                requestDto.getFinishedProductName(), requestDto.getQuantity());

        BillOfMaterial bom = billOfMaterialRepository
                .findByFinishedProductNameIgnoreCase(requestDto.getFinishedProductName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BOM not found for finished product: " + requestDto.getFinishedProductName()));

        FinishedProduct finishedProduct = finishedProductRepository.findById(bom.getFinishedProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Finished product not found with ID: " + bom.getFinishedProductId()));

        BigDecimal requestedQuantity = requestDto.getQuantity();
        BigDecimal bomOutputQuantity = bom.getOutputQuantity();

        BigDecimal ratio = requestedQuantity.divide(bomOutputQuantity, 4, RoundingMode.HALF_UP);
        log.debug("Production ratio calculated: {} (requested: {}, BOM output: {})",
                ratio, requestedQuantity, bomOutputQuantity);

        List<BomProductionResponseDto.RawMaterialConsumptionDto> consumptions =
                new java.util.ArrayList<>();

        for (BomComponent component : bom.getComponents()) {
            BigDecimal requiredQuantity = component.getQuantity().multiply(ratio)
                    .setScale(4, RoundingMode.HALF_UP);

            // Use findActiveById to ensure we only work with active raw products
            RawProduct rawProduct = rawProductRepository.findActiveById(component.getRawMaterialId())
                    .orElseThrow(() -> new RawProductNotFoundException(component.getRawMaterialId()));

            // Use BigDecimal for exact quantity tracking
            BigDecimal currentQty = rawProduct.getQuantity() != null ? rawProduct.getQuantity() : BigDecimal.ZERO;

            if (currentQty.compareTo(requiredQuantity) < 0) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for raw material '%s'. Required: %s %s, Available: %s %s",
                                rawProduct.getName(), requiredQuantity, component.getUnit(),
                                currentQty, rawProduct.getUnitName() != null ? rawProduct.getUnitName() : component.getUnit()));
            }

            // Calculate new quantity with exact decimal math (no rounding)
            BigDecimal newQuantity = currentQty.subtract(requiredQuantity);

            rawProduct.setQuantity(newQuantity);
            rawProductRepository.save(rawProduct);
            deductFromFifoLots(rawProduct.getId(), requiredQuantity);
            log.info("Deducted {} {} from raw material: {} (ID: {}). New stock: {}",
                    requiredQuantity, component.getUnit(), rawProduct.getName(),
                    rawProduct.getId(), rawProduct.getQuantity());

            consumptions.add(BomProductionResponseDto.RawMaterialConsumptionDto.builder()
                    .rawMaterialId(rawProduct.getId())
                    .rawMaterialName(rawProduct.getName())
                    .quantityRequired(requiredQuantity)
                    .quantityAvailable(currentQty)
                    .unit(component.getUnit())
                    .newStock(newQuantity)
                    .build());
        }

        // Handle finished product stock with BigDecimal
        BigDecimal currentFinishedQty = finishedProduct.getQuantity() != null ?
                BigDecimal.valueOf(finishedProduct.getQuantity()) : BigDecimal.ZERO;
        BigDecimal newFinishedProductStock = currentFinishedQty.add(requestedQuantity);
        finishedProduct.setQuantity(newFinishedProductStock.intValue());
        finishedProduct.setStatus(ProductStatus.BOM);

        if (requestDto.getBatchNumber() != null) {
            finishedProduct.setBatchNumber(requestDto.getBatchNumber());
        }

        finishedProductRepository.save(finishedProduct);
        log.info("Added {} {} of finished product: {}. New stock: {}",
                requestedQuantity, bom.getOutputUnit(), finishedProduct.getName(), newFinishedProductStock);

        return BomProductionResponseDto.builder()
                .bomId(bom.getId())
                .finishedProductName(finishedProduct.getName())
                .finishedProductId(finishedProduct.getId())
                .quantityProduced(requestedQuantity)
                .outputUnit(bom.getOutputUnit())
                .finishedProductNewStock(newFinishedProductStock.intValue())
                .batchNumber(finishedProduct.getBatchNumber())
                .rawMaterialsConsumed(consumptions)
                .productionTime(Instant.now())
                .message(String.format("Successfully produced %s %s of '%s' using BOM '%s'",
                        requestedQuantity, bom.getOutputUnit(), finishedProduct.getName(), bom.getBomName()))
                .build();
    }
}
