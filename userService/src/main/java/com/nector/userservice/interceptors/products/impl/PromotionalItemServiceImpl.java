package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.PromotionalItemNotFoundException;
import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.PromotionalItemResponse;
import com.nector.userservice.interceptors.products.service.ProductPriceHistoryService;
import com.nector.userservice.interceptors.products.service.PromotionalItemService;
import com.nector.userservice.model.PromotionalItem;
import com.nector.userservice.repository.PromotionalItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionalItemServiceImpl implements PromotionalItemService {
    
    private final PromotionalItemRepository promotionalItemRepository;
    private final ProductPriceHistoryService productPriceHistoryService;
    
    @Override
    @Transactional
    public PromotionalItemResponse createPromotionalItem(PromotionalItemRequest request) {
        log.info("Creating promotional item with item code: {}", request.getItemCode());
        
        if (request.getItemCode() != null && promotionalItemRepository.existsByItemCode(request.getItemCode())) {
            throw new DataIntegrityViolationException("Promotional item with item code " + request.getItemCode() + " already exists");
        }
        
        PromotionalItem item = new PromotionalItem();
        item.setName(request.getName());
        item.setItemCode(request.getItemCode());
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setMinimumThreshold(request.getMinimumThreshold());
        item.setHsn(request.getHsn());
        item.setTaxRate(request.getTaxRate());
        item.setVendorId(request.getVendorId());
        item.setVendorName(request.getVendorName());
        item.setTransportName(request.getTransportName());
        item.setDriverName(request.getDriverName());
        item.setDriverMobile(request.getDriverMobile());
        item.setActive(true);
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        
        PromotionalItem savedItem = promotionalItemRepository.save(item);
        log.info("Promotional item created successfully with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem);
    }
    
    @Override
    @Transactional
    public PromotionalItemResponse updatePromotionalItem(Long id, PromotionalItemRequest request) {
        log.info("Updating promotional item with ID: {}", id);
        
        PromotionalItem item = promotionalItemRepository.findById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));

        BigDecimal oldPrice = item.getPrice();

        item.setName(request.getName());
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setMinimumThreshold(request.getMinimumThreshold());
        item.setHsn(request.getHsn());
        item.setTaxRate(request.getTaxRate());
        item.setVendorId(request.getVendorId());
        item.setVendorName(request.getVendorName());
        item.setTransportName(request.getTransportName());
        item.setDriverName(request.getDriverName());
        item.setDriverMobile(request.getDriverMobile());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        log.info("Promotional item updated successfully with ID: {}", updatedItem.getId());

        BigDecimal newPrice = updatedItem.getPrice();
        if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
            productPriceHistoryService.record(ProductPriceHistory.ProductType.PROMOTIONAL_ITEM,
                    updatedItem.getId(), updatedItem.getName(), updatedItem.getItemCode(),
                    oldPrice, newPrice);
        }

        return mapToResponse(updatedItem);
    }

    @Override
    @Transactional
    public void deletePromotionalItem(Long id) {
        log.info("Soft deleting promotional item with ID: {}", id);
        
        PromotionalItem item = promotionalItemRepository.findById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        item.setActive(false);
        promotionalItemRepository.save(item);
        
        log.info("Promotional item soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PromotionalItemResponse getPromotionalItemById(Long id) {
        log.info("Fetching promotional item with ID: {}", id);
        
        PromotionalItem item = promotionalItemRepository.findById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        return mapToResponse(item);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PromotionalItemResponse> getAllPromotionalItems() {
        log.info("Fetching all promotional items");
        
        return promotionalItemRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public PromotionalItemResponse increaseStock(Long id, BigDecimal quantity) {
        log.info("Increasing stock for promotional item ID: {} by quantity: {}", id, quantity);
        
        PromotionalItem item = promotionalItemRepository.findActiveById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        BigDecimal currentQty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
        item.setQuantity(currentQty.add(quantity).intValue());
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        
        log.info("Stock increased successfully for promotional item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional
    public PromotionalItemResponse decreaseStock(Long id, BigDecimal quantity) {
        log.info("Decreasing stock for promotional item ID: {} by quantity: {}", id, quantity);
        
        PromotionalItem item = promotionalItemRepository.findActiveById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        BigDecimal currentQty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
        if (currentQty.compareTo(quantity) < 0) {
            throw new InsufficientStockException(item.getItemCode(), quantity, currentQty);
        }
        
        item.setQuantity(currentQty.subtract(quantity).intValue());
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        
        int updatedQty = updatedItem.getQuantity() != null ? updatedItem.getQuantity() : 0;
        int threshold = updatedItem.getMinimumThreshold() != null ? updatedItem.getMinimumThreshold() : 0;
        if (updatedQty <= threshold) {
            log.warn("ALERT: Promotional item {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}", 
                updatedItem.getItemCode(), id, updatedItem.getQuantity(), updatedItem.getMinimumThreshold());
        }
        
        log.info("Stock decreased successfully for promotional item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PromotionalItemResponse> getLowStockItems() {
        log.info("Fetching low stock promotional items");
        
        return promotionalItemRepository.findLowStockItems().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private PromotionalItemResponse mapToResponse(PromotionalItem item) {
        PromotionalItemResponse response = new PromotionalItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setItemCode(item.getItemCode());
        response.setUnit(item.getUnit());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        
        BigDecimal perItemPrice = BigDecimal.ZERO;
        if (item.getQuantity() != null && item.getQuantity() > 0 && item.getPrice() != null) {
            perItemPrice = item.getPrice()
                .divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP);
        }
        response.setPerItemPrice(perItemPrice);
        
        response.setMinimumThreshold(item.getMinimumThreshold());
        response.setActive(item.getActive());
        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
        int minThreshold = item.getMinimumThreshold() != null ? item.getMinimumThreshold() : 0;
        response.setLowStock(qty <= minThreshold);
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        response.setVendorId(item.getVendorId());
        response.setVendorName(item.getVendorName());
        response.setTransportName(item.getTransportName());
        response.setDriverName(item.getDriverName());
        response.setDriverMobile(item.getDriverMobile());
        response.setStatus(item.getStatus());
        return response;
    }

    @Override
    @Transactional
    public PromotionalItemResponse updateByItemCode(String itemCode, PromotionalItemRequest request) {
        log.info("Updating promotional item by item code: {}", itemCode);

        PromotionalItem item = promotionalItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new PromotionalItemNotFoundException(0L));

        BigDecimal oldPriceByCode = item.getPrice();

        if (request.getName() != null) item.setName(request.getName());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) item.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getHsn() != null) item.setHsn(request.getHsn());
        if (request.getTaxRate() != null) item.setTaxRate(request.getTaxRate());
        if (request.getVendorId() != null) item.setVendorId(request.getVendorId());
        if (request.getVendorName() != null) item.setVendorName(request.getVendorName());
        if (request.getTransportName() != null) item.setTransportName(request.getTransportName());
        if (request.getDriverName() != null) item.setDriverName(request.getDriverName());
        if (request.getDriverMobile() != null) item.setDriverMobile(request.getDriverMobile());
        if (request.getStatus() != null) item.setStatus(request.getStatus());

        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        log.info("Promotional item updated by item code: {}", itemCode);

        BigDecimal newPriceByCode = updatedItem.getPrice();
        if (request.getPrice() != null && oldPriceByCode != null
                && oldPriceByCode.compareTo(newPriceByCode) != 0) {
            productPriceHistoryService.record(ProductPriceHistory.ProductType.PROMOTIONAL_ITEM,
                    updatedItem.getId(), updatedItem.getName(), updatedItem.getItemCode(),
                    oldPriceByCode, newPriceByCode);
        }

        return mapToResponse(updatedItem);
    }
}
