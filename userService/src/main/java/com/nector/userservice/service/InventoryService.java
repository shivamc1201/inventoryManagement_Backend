package com.nector.userservice.service;

import com.nector.userservice.dto.inventory.ItemRequest;
import com.nector.userservice.dto.inventory.ItemResponse;
import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final FinishedProductRepository finishedProductRepository;
    
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
         log.info("Creating item with SKU: {}", request.getSku());
        
        if (finishedProductRepository.existsBySku(request.getSku())) {
            throw new DataIntegrityViolationException("Item with SKU " + request.getSku() + " already exists");
        }
        
        FinishedProduct finishedProduct = new FinishedProduct();
        finishedProduct.setName(request.getName());
        finishedProduct.setDescription(request.getDescription());
        finishedProduct.setSku(request.getSku());
        finishedProduct.setPrice(request.getPrice());
        finishedProduct.setQuantity(request.getQuantity());
        
        FinishedProduct savedItem = finishedProductRepository.save(finishedProduct);
        log.info("Item created successfully with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem);
    }
    
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        log.info("Updating item with ID: {}", id);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        finishedProduct.setName(request.getName());
        finishedProduct.setDescription(request.getDescription());
        finishedProduct.setPrice(request.getPrice());
        finishedProduct.setQuantity(request.getQuantity());
        
        FinishedProduct updatedItem = finishedProductRepository.save(finishedProduct);
        log.info("Item updated successfully with ID: {}", updatedItem.getId());
        
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public void deleteItem(Long id) {
        log.info("Soft deleting item with ID: {}", id);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        finishedProduct.setActive(false);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Item soft deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        log.info("Fetching item with ID: {}", id);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        return mapToResponse(finishedProduct);
    }
    
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        log.info("Fetching all items");
        
        return finishedProductRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ItemResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for item ID: {} by quantity: {}", id, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() + quantity);
        FinishedProduct updatedItem = finishedProductRepository.save(finishedProduct);
        
        log.info("Stock increased successfully for item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public ItemResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for item ID: {} by quantity: {}", id, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        if (finishedProduct.getQuantity() < quantity) {
            throw new InsufficientStockException(finishedProduct.getSku(), quantity, finishedProduct.getQuantity());
        }
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() - quantity);
        FinishedProduct updatedItem = finishedProductRepository.save(finishedProduct);
        
        log.info("Stock decreased successfully for item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public void reserveStock(Long itemId, Integer quantity) {
        log.info("Reserving stock for item ID: {} quantity: {}", itemId, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findActiveById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        if (finishedProduct.getQuantity() < quantity) {
            throw new InsufficientStockException(finishedProduct.getSku(), quantity, finishedProduct.getQuantity());
        }
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() - quantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock reserved successfully for item ID: {}", itemId);
    }
    
    @Transactional
    public void releaseStock(Long itemId, Integer quantity) {
        log.info("Releasing stock for item ID: {} quantity: {}", itemId, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() + quantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock released successfully for item ID: {}", itemId);
    }
    
    @Transactional
    public void updateStock(Long itemId, Integer quantityChange) {
        log.info("Updating stock for item ID: {} by: {}", itemId, quantityChange);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        int newQuantity = finishedProduct.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InsufficientStockException(finishedProduct.getSku(), Math.abs(quantityChange), finishedProduct.getQuantity());
        }
        
        finishedProduct.setQuantity(newQuantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock updated successfully for item ID: {}", itemId);
    }

    @Transactional(readOnly = true)
    public Integer getAvailableStock(Long itemId) {
        log.info("Getting available stock for item ID: {}", itemId);
        
        FinishedProduct finishedProduct = finishedProductRepository.findById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        return finishedProduct.getQuantity();
    }

    @Transactional(readOnly = true)
    public Integer getAvailableStockBySku(String sku) {
        log.info("Getting available stock for item SKU: {}", sku);

        FinishedProduct finishedProduct = finishedProductRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Item not found with SKU: " + sku));

        return finishedProduct.getQuantity();
    }

    @Transactional
    public void updateStockBySku(String sku, Integer quantityChange) {
        log.info("Updating stock for item SKU: {} by: {}", sku, quantityChange);
        
        FinishedProduct finishedProduct = finishedProductRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Item not found with SKU: " + sku));
        
        int newQuantity = finishedProduct.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InsufficientStockException(finishedProduct.getSku(), Math.abs(quantityChange), finishedProduct.getQuantity());
        }
        
        finishedProduct.setQuantity(newQuantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock updated successfully for item SKU: {}", sku);
    }

    @Transactional
    public void reserveStockBySku(String sku, Integer quantity) {
        log.info("Reserving stock for item SKU: {} quantity: {}", sku, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Item not found with SKU: " + sku));
        
        if (finishedProduct.getQuantity() < quantity) {
            throw new InsufficientStockException(finishedProduct.getSku(), quantity, finishedProduct.getQuantity());
        }
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() - quantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock reserved successfully for item SKU: {}", sku);
    }

    @Transactional
    public void releaseStockBySku(String sku, Integer quantity) {
        log.info("Releasing stock for item SKU: {} quantity: {}", sku, quantity);
        
        FinishedProduct finishedProduct = finishedProductRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Item not found with SKU: " + sku));
        
        finishedProduct.setQuantity(finishedProduct.getQuantity() + quantity);
        finishedProductRepository.save(finishedProduct);
        
        log.info("Stock released successfully for item SKU: {}", sku);
    }
    
    private ItemResponse mapToResponse(FinishedProduct finishedProduct) {
        ItemResponse response = new ItemResponse();
        response.setId(finishedProduct.getId());
        response.setName(finishedProduct.getName());
        response.setDescription(finishedProduct.getDescription());
        response.setSku(finishedProduct.getSku());
        response.setPrice(finishedProduct.getPrice());
        response.setQuantity(finishedProduct.getQuantity());
        response.setActive(finishedProduct.getActive());
        response.setCreatedAt(finishedProduct.getCreatedAt());
        response.setUpdatedAt(finishedProduct.getUpdatedAt());
        return response;
    }
}
