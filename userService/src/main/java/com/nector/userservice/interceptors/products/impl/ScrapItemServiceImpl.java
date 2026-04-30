package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.ScrapItemNotFoundException;
import com.nector.userservice.interceptors.products.model.ScrapItemRequest;
import com.nector.userservice.interceptors.products.model.ScrapItemResponse;
import com.nector.userservice.interceptors.products.service.ScrapItemService;
import com.nector.userservice.model.ScrapItem;
import com.nector.userservice.repository.ScrapItemRepository;
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
public class ScrapItemServiceImpl implements ScrapItemService {
    
    private final ScrapItemRepository scrapItemRepository;
    
    @Override
    @Transactional
    public ScrapItemResponse createScrapItem(ScrapItemRequest request) {
        log.info("Creating scrap item with item code: {}", request.getItemCode());
        
        if (request.getItemCode() != null && scrapItemRepository.existsByItemCode(request.getItemCode())) {
            throw new DataIntegrityViolationException("Scrap item with item code " + request.getItemCode() + " already exists");
        }
        
        ScrapItem item = new ScrapItem();
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
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        
        ScrapItem savedItem = scrapItemRepository.save(item);
        log.info("Scrap item created successfully with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem);
    }
    
    @Override
    @Transactional
    public ScrapItemResponse updateScrapItem(Long id, ScrapItemRequest request) {
        log.info("Updating scrap item with ID: {}", id);
        
        ScrapItem item = scrapItemRepository.findById(id)
            .orElseThrow(() -> new ScrapItemNotFoundException(id));
        
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
        
        ScrapItem updatedItem = scrapItemRepository.save(item);
        log.info("Scrap item updated successfully with ID: {}", updatedItem.getId());
        
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional
    public void deleteScrapItem(Long id) {
        log.info("Soft deleting scrap item with ID: {}", id);
        
        ScrapItem item = scrapItemRepository.findById(id)
            .orElseThrow(() -> new ScrapItemNotFoundException(id));
        
        item.setActive(false);
        scrapItemRepository.save(item);
        
        log.info("Scrap item soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ScrapItemResponse getScrapItemById(Long id) {
        log.info("Fetching scrap item with ID: {}", id);
        
        ScrapItem item = scrapItemRepository.findById(id)
            .orElseThrow(() -> new ScrapItemNotFoundException(id));
        
        return mapToResponse(item);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScrapItemResponse> getAllScrapItems() {
        log.info("Fetching all scrap items");
        
        return scrapItemRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ScrapItemResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for scrap item ID: {} by quantity: {}", id, quantity);
        
        ScrapItem item = scrapItemRepository.findActiveById(id)
            .orElseThrow(() -> new ScrapItemNotFoundException(id));
        
        int currentQty = item.getQuantity() != null ? item.getQuantity() : 0;
        item.setQuantity(currentQty + quantity);
        ScrapItem updatedItem = scrapItemRepository.save(item);
        
        log.info("Stock increased successfully for scrap item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional
    public ScrapItemResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for scrap item ID: {} by quantity: {}", id, quantity);
        
        ScrapItem item = scrapItemRepository.findActiveById(id)
            .orElseThrow(() -> new ScrapItemNotFoundException(id));
        
        int currentQty = item.getQuantity() != null ? item.getQuantity() : 0;
        if (currentQty < quantity) {
            throw new InsufficientStockException(item.getItemCode(), quantity, currentQty);
        }
        
        item.setQuantity(currentQty - quantity);
        ScrapItem updatedItem = scrapItemRepository.save(item);
        
        int updatedQty = updatedItem.getQuantity() != null ? updatedItem.getQuantity() : 0;
        int threshold = updatedItem.getMinimumThreshold() != null ? updatedItem.getMinimumThreshold() : 0;
        if (updatedQty <= threshold) {
            log.warn("ALERT: Scrap item {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}", 
                updatedItem.getItemCode(), id, updatedItem.getQuantity(), updatedItem.getMinimumThreshold());
        }
        
        log.info("Stock decreased successfully for scrap item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScrapItemResponse> getLowStockItems() {
        log.info("Fetching low stock scrap items");
        
        return scrapItemRepository.findLowStockItems().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private ScrapItemResponse mapToResponse(ScrapItem item) {
        ScrapItemResponse response = new ScrapItemResponse();
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
    public ScrapItemResponse updateByItemCode(String itemCode, ScrapItemRequest request) {
        log.info("Updating scrap item by item code: {}", itemCode);

        ScrapItem item = scrapItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ScrapItemNotFoundException(0L));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getUnit() != null) item.setUnit(request.getUnit());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) item.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getVendorId() != null) item.setVendorId(request.getVendorId());
        if (request.getVendorName() != null) item.setVendorName(request.getVendorName());
        if (request.getTransportName() != null) item.setTransportName(request.getTransportName());
        if (request.getDriverName() != null) item.setDriverName(request.getDriverName());
        if (request.getDriverMobile() != null) item.setDriverMobile(request.getDriverMobile());
        if (request.getStatus() != null) item.setStatus(request.getStatus());

        ScrapItem updatedItem = scrapItemRepository.save(item);
        log.info("Scrap item updated by item code: {}", itemCode);
        return mapToResponse(updatedItem);
    }
}
