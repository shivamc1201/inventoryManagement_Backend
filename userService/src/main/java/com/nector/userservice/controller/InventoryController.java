package com.nector.userservice.controller;

import com.nector.userservice.dto.inventory.ItemRequest;
import com.nector.userservice.dto.inventory.ItemResponse;
import com.nector.userservice.dto.inventory.StockUpdateRequest;
import com.nector.userservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping("/create")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        log.info("Creating new item with SKU: {}", request.getSku());
        ItemResponse response = inventoryService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        log.info("Updating item with ID: {}", id);
        ItemResponse response = inventoryService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) {
        log.info("Deleting item with ID: {}", id);
        inventoryService.deleteItem(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Item deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        log.info("Fetching item with ID: {}", id);
        ItemResponse response = inventoryService.getItemById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        log.info("Fetching all items");
        List<ItemResponse> response = inventoryService.getAllItems();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<ItemResponse> increaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Increasing stock for item ID: {} by quantity: {}", id, request.getQuantity());
        ItemResponse response = inventoryService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/decrease-stock")
    public ResponseEntity<ItemResponse> decreaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Decreasing stock for item ID: {} by quantity: {}", id, request.getQuantity());
        ItemResponse response = inventoryService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
}