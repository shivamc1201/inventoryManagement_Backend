package com.nector.userservice.service;

import com.nector.userservice.dto.inventory.ItemRequest;
import com.nector.userservice.dto.inventory.ItemResponse;
import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.model.Item;
import com.nector.userservice.repository.ItemRepository;
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
    
    private final ItemRepository itemRepository;
    
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        log.info("Creating item with SKU: {}", request.getSku());
        
        if (itemRepository.existsBySku(request.getSku())) {
            throw new DataIntegrityViolationException("Item with SKU " + request.getSku() + " already exists");
        }
        
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setSku(request.getSku());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        
        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with ID: {}", savedItem.getId());
        
        return mapToResponse(savedItem);
    }
    
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        log.info("Updating item with ID: {}", id);
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        
        Item updatedItem = itemRepository.save(item);
        log.info("Item updated successfully with ID: {}", updatedItem.getId());
        
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public void deleteItem(Long id) {
        log.info("Soft deleting item with ID: {}", id);
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        item.setActive(false);
        itemRepository.save(item);
        
        log.info("Item soft deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        log.info("Fetching item with ID: {}", id);
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        return mapToResponse(item);
    }
    
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        log.info("Fetching all items");
        
        return itemRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ItemResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for item ID: {} by quantity: {}", id, quantity);
        
        Item item = itemRepository.findActiveById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        item.setQuantity(item.getQuantity() + quantity);
        Item updatedItem = itemRepository.save(item);
        
        log.info("Stock increased successfully for item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public ItemResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for item ID: {} by quantity: {}", id, quantity);
        
        Item item = itemRepository.findActiveById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        if (item.getQuantity() < quantity) {
            throw new InsufficientStockException(item.getSku(), quantity, item.getQuantity());
        }
        
        item.setQuantity(item.getQuantity() - quantity);
        Item updatedItem = itemRepository.save(item);
        
        log.info("Stock decreased successfully for item ID: {}", id);
        return mapToResponse(updatedItem);
    }
    
    @Transactional
    public void reserveStock(Long itemId, Integer quantity) {
        log.info("Reserving stock for item ID: {} quantity: {}", itemId, quantity);
        
        Item item = itemRepository.findActiveById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        if (item.getQuantity() < quantity) {
            throw new InsufficientStockException(item.getSku(), quantity, item.getQuantity());
        }
        
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);
        
        log.info("Stock reserved successfully for item ID: {}", itemId);
    }
    
    @Transactional
    public void releaseStock(Long itemId, Integer quantity) {
        log.info("Releasing stock for item ID: {} quantity: {}", itemId, quantity);
        
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        item.setQuantity(item.getQuantity() + quantity);
        itemRepository.save(item);
        
        log.info("Stock released successfully for item ID: {}", itemId);
    }
    
    private ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setSku(item.getSku());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setActive(item.getActive());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}