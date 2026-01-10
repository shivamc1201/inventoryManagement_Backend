package com.nector.userservice.controller;

import com.nector.userservice.dto.finishedproduct.FinishedProductRequest;
import com.nector.userservice.dto.finishedproduct.FinishedProductResponse;
import com.nector.userservice.dto.inventory.StockUpdateRequest;
import com.nector.userservice.service.FinishedProductService;
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
@RequestMapping("/api/inventory/finished-products")
@RequiredArgsConstructor
@Slf4j
public class FinishedProductController {
    
    private final FinishedProductService finishedProductService;
    
    @PostMapping("/create-item")
    public ResponseEntity<FinishedProductResponse> createFinishedProduct(@Valid @RequestBody FinishedProductRequest request) {
        log.info("Creating new finished product with SKU: {}", request.getSku());
        FinishedProductResponse response = finishedProductService.createFinishedProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/edit-item/{id}")
    public ResponseEntity<FinishedProductResponse> updateFinishedProduct(@PathVariable Long id, @Valid @RequestBody FinishedProductRequest request) {
        log.info("Updating finished product with ID: {}", id);
        FinishedProductResponse response = finishedProductService.updateFinishedProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete-item/{id}")
    public ResponseEntity<Map<String, String>> deleteFinishedProduct(@PathVariable Long id) {
        log.info("Deleting finished product with ID: {}", id);
        finishedProductService.deleteFinishedProduct(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Finished product deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-item/{id}")
    public ResponseEntity<FinishedProductResponse> getFinishedProductById(@PathVariable Long id) {
        log.info("Fetching finished product with ID: {}", id);
        FinishedProductResponse response = finishedProductService.getFinishedProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-all-items")
    public ResponseEntity<List<FinishedProductResponse>> getAllFinishedProducts() {
        log.info("Fetching all finished products");
        List<FinishedProductResponse> response = finishedProductService.getAllFinishedProducts();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/increase-stock/{id}")
    public ResponseEntity<FinishedProductResponse> increaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Increasing stock for finished product ID: {} by quantity: {}", id, request.getQuantity());
        FinishedProductResponse response = finishedProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/decrease-stock/{id}")
    public ResponseEntity<FinishedProductResponse> decreaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Decreasing stock for finished product ID: {} by quantity: {}", id, request.getQuantity());
        FinishedProductResponse response = finishedProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-low-stock-items")
    public ResponseEntity<List<FinishedProductResponse>> getLowStockItems() {
        log.info("Fetching low stock finished products");
        List<FinishedProductResponse> response = finishedProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }
}