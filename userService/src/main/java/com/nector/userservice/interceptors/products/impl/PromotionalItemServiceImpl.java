package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.PromotionalItemNotFoundException;
import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.PromotionalItemResponse;
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
    
    @Override
    @Transactional
    public PromotionalItemResponse createPromotionalItem(PromotionalItemRequest request) {
        log.info("Creating promotional item with item code: {}", request.getItemCode());
        
        if (promotionalItemRepository.existsByItemCode(request.getItemCode())) {
            throw new DataIntegrityViolationException("Promotional item with item code " + request.getItemCode() + " already exists");
        }
        
        PromotionalItem item = new PromotionalItem();
        item.setName(request.getName());
        item.setItemCode(request.getItemCode());
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setMinimumThreshold(request.getMinimumThreshold());
        item.setVendorId(request.getVendorId());
        item.setVendorName(request.getVendorName());
        item.setTransportName(request.getTransportName());
        item.setDriverName(request.getDriverName());
        item.setDriverMobile(request.getDriverMobile());
        item.setActive(true);
        
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
        
        item.setName(request.getName());
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setMinimumThreshold(request.getMinimumThreshold());
        item.setVendorId(request.getVendorId());
        item.setVendorName(request.getVendorName());
        item.setTransportName(request.getTransportName());
        item.setDriverName(request.getDriverName());
        item.setDriverMobile(request.getDriverMobile());
        
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        log.info("Promotional item updated successfully with ID: {}", updatedItem.getId());
        
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
    public PromotionalItemResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for promotional item ID: {} by quantity: {}", id, quantity);
        
        PromotionalItem item = promotionalItemRepository.findActiveById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        item.setQuantity(item.getQuantity() + quantity);
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        
        log.info("Stock increased successfully for promotional item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional
    public PromotionalItemResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for promotional item ID: {} by quantity: {}", id, quantity);
        
        PromotionalItem item = promotionalItemRepository.findActiveById(id)
            .orElseThrow(() -> new PromotionalItemNotFoundException(id));
        
        if (item.getQuantity() < quantity) {
            throw new InsufficientStockException(item.getItemCode(), quantity, item.getQuantity());
        }
        
        item.setQuantity(item.getQuantity() - quantity);
        PromotionalItem updatedItem = promotionalItemRepository.save(item);
        
        if (updatedItem.getQuantity() <= updatedItem.getMinimumThreshold()) {
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
        response.setLowStock(item.getQuantity() <= item.getMinimumThreshold());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        response.setVendorId(item.getVendorId());
        response.setVendorName(item.getVendorName());
        response.setTransportName(item.getTransportName());
        response.setDriverName(item.getDriverName());
        response.setDriverMobile(item.getDriverMobile());
        return response;
    }
}
