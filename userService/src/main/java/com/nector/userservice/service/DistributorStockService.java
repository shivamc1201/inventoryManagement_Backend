package com.nector.userservice.service;

import com.nector.userservice.model.DistributorStock;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.DistributorStockRepository;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributorStockService {

    private final DistributorStockRepository distributorStockRepository;
    private final FinishedProductRepository finishedProductRepository;

    /**
     * Add stock to distributor inventory when receiving from company
     * (+ stock operation)
     */
    @Transactional
    public void addStock(Long distributorId, Long itemId, String sku, Integer quantity) {
        log.info("Adding stock for distributor: {}, itemId: {}, sku: {}, quantity: {}", 
                distributorId, itemId, sku, quantity);

        if (quantity <= 0) {
            log.warn("Cannot add zero or negative quantity: {}", quantity);
            return;
        }

        // Get item details from FinishedProduct
        String itemName = null;
        String unitType = null;
        
        Optional<FinishedProduct> productOpt = finishedProductRepository.findById(itemId);
        if (productOpt.isPresent()) {
            FinishedProduct product = productOpt.get();
            itemName = product.getName();
            unitType = product.getUnitType() != null ? product.getUnitType() : 
                      (product.getUnit() != null ? product.getUnit().name() : null);
            
            // Use SKU from product if not provided
            if (sku == null || sku.trim().isEmpty()) {
                sku = product.getSku();
            }
        }

        if (sku == null || sku.trim().isEmpty()) {
            throw new BusinessException("SKU is required for stock addition");
        }

        // Use pessimistic locking to prevent race conditions
        Optional<DistributorStock> existingStockOpt = distributorStockRepository
                .findByDistributorIdAndSkuForUpdate(distributorId, sku);

        if (existingStockOpt.isPresent()) {
            DistributorStock stock = existingStockOpt.get();
            int newQuantity = stock.getQuantity() + quantity;
            stock.setQuantity(newQuantity);
            distributorStockRepository.save(stock);
            log.info("Updated existing stock for SKU: {}. New quantity: {}", sku, newQuantity);
        } else {
            DistributorStock newStock = DistributorStock.builder()
                    .distributorId(distributorId)
                    .itemId(itemId)
                    .sku(sku)
                    .itemName(itemName)
                    .quantity(quantity)
                    .unitType(unitType)
                    .build();
            distributorStockRepository.save(newStock);
            log.info("Created new stock entry for SKU: {} with quantity: {}", sku, quantity);
        }
    }

    /**
     * Reduce stock from distributor inventory when billing to dealer
     * (- stock operation)
     */
    @Transactional
    public void reduceStock(Long distributorId, String sku, Integer quantity) {
        log.info("Reducing stock for distributor: {}, sku: {}, quantity: {}", 
                distributorId, sku, quantity);

        if (quantity <= 0) {
            log.warn("Cannot reduce zero or negative quantity: {}", quantity);
            return;
        }

        if (sku == null || sku.trim().isEmpty()) {
            throw new BusinessException("SKU is required for stock reduction");
        }

        String normalizedSku = sku.trim();

        // Use pessimistic locking to prevent race conditions during concurrent billing
        Optional<DistributorStock> stockOpt = distributorStockRepository
                .findByDistributorIdAndSkuForUpdate(distributorId, normalizedSku);

        if (stockOpt.isEmpty()) {
            throw new BusinessException(
                "Stock not found for SKU: " + normalizedSku + ". Distributor must receive stock from company first.");
        }

        DistributorStock stock = stockOpt.get();
        
        if (stock.getQuantity() < quantity) {
            throw new BusinessException(
                "Insufficient stock for SKU: " + normalizedSku + 
                ". Available: " + stock.getQuantity() + ", Requested: " + quantity);
        }

        int newQuantity = stock.getQuantity() - quantity;
        stock.setQuantity(newQuantity);
        distributorStockRepository.save(stock);
        
        log.info("Reduced stock for SKU: {}. New quantity: {}", normalizedSku, newQuantity);
    }

    /**
     * Get current stock quantity for a specific SKU
     */
    @Transactional(readOnly = true)
    public Integer getStockQuantity(Long distributorId, String sku) {
        return distributorStockRepository
                .findByDistributorIdAndSku(distributorId, sku)
                .map(DistributorStock::getQuantity)
                .orElse(0);
    }

    /**
     * Check if sufficient stock is available
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(Long distributorId, String sku, Integer requiredQuantity) {
        Integer currentStock = getStockQuantity(distributorId, sku);
        return currentStock >= requiredQuantity;
    }

    /**
     * Get all stocks for a distributor
     */
    @Transactional(readOnly = true)
    public List<DistributorStock> getStocksByDistributorId(Long distributorId) {
        return distributorStockRepository.findByDistributorId(distributorId);
    }
}
