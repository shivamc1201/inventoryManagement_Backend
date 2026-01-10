package com.nector.userservice.controller;

import com.nector.userservice.dto.inventory.StockUpdateRequest;
import com.nector.userservice.dto.rawproduct.RawProductRequest;
import com.nector.userservice.dto.rawproduct.RawProductResponse;
import com.nector.userservice.service.RawProductService;
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
@RequestMapping("/api/inventory/raw-materials")
@RequiredArgsConstructor
@Slf4j
public class RawProductController {
    
    private final RawProductService rawProductService;
    
    @PostMapping("/create-material")
    public ResponseEntity<RawProductResponse> createRawProduct(@Valid @RequestBody RawProductRequest request) {
        log.info("Creating new raw product with material code: {}", request.getMaterialCode());
        RawProductResponse response = rawProductService.createRawProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/edit-material/{id}")
    public ResponseEntity<RawProductResponse> updateRawProduct(@PathVariable Long id, @Valid @RequestBody RawProductRequest request) {
        log.info("Updating raw product with ID: {}", id);
        RawProductResponse response = rawProductService.updateRawProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete-material/{id}")
    public ResponseEntity<Map<String, String>> deleteRawProduct(@PathVariable Long id) {
        log.info("Deleting raw product with ID: {}", id);
        rawProductService.deleteRawProduct(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Raw product deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-material/{id}")
    public ResponseEntity<RawProductResponse> getRawProductById(@PathVariable Long id) {
        log.info("Fetching raw product with ID: {}", id);
        RawProductResponse response = rawProductService.getRawProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-all-materials")
    public ResponseEntity<List<RawProductResponse>> getAllRawProducts() {
        log.info("Fetching all raw products");
        List<RawProductResponse> response = rawProductService.getAllRawProducts();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/increase-stock/{id}")
    public ResponseEntity<RawProductResponse> increaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Increasing stock for raw product ID: {} by quantity: {}", id, request.getQuantity());
        RawProductResponse response = rawProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/decrease-stock/{id}")
    public ResponseEntity<RawProductResponse> decreaseStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        log.info("Decreasing stock for raw product ID: {} by quantity: {}", id, request.getQuantity());
        RawProductResponse response = rawProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-low-stock-materials")
    public ResponseEntity<List<RawProductResponse>> getLowStockItems() {
        log.info("Fetching low stock raw products");
        List<RawProductResponse> response = rawProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }
}