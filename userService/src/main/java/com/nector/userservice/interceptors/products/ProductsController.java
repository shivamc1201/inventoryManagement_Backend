package com.nector.userservice.interceptors.products;

import com.nector.userservice.dto.inventory.StockUpdateRequest;
import com.nector.userservice.interceptors.products.model.*;
import com.nector.userservice.interceptors.products.service.FinishedProductService;
import com.nector.userservice.interceptors.products.service.MachinePartService;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.model.MachinePart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "APIs for product management including finished products, raw materials, and machine parts")
public class ProductsController {
    
    private final FinishedProductService finishedProductService;
    private final RawProductService rawProductService;
    private final MachinePartService machinePartService;
    
    // === FINISHED PRODUCTS ===
    
    @PostMapping("/finished-products")
    @Operation(summary = "Create finished product", description = "Creates a new finished product")
    public ResponseEntity<FinishedProductResponse> createFinishedProduct(@Valid @RequestBody FinishedProductRequest request) {
        FinishedProductResponse response = finishedProductService.createFinishedProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/finished-products")
    @Operation(summary = "Get all finished products", description = "Retrieves all finished products")
    public ResponseEntity<List<FinishedProductResponse>> getAllFinishedProducts() {
        List<FinishedProductResponse> response = finishedProductService.getAllFinishedProducts();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/finished-products/{id}")
    @Operation(summary = "Get finished product by ID", description = "Retrieves a finished product by its ID")
    public ResponseEntity<FinishedProductResponse> getFinishedProductById(@PathVariable Long id) {
        FinishedProductResponse response = finishedProductService.getFinishedProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/finished-products/{id}")
    @Operation(summary = "Update finished product", description = "Updates an existing finished product")
    public ResponseEntity<FinishedProductResponse> updateFinishedProduct(@PathVariable Long id, @Valid @RequestBody FinishedProductRequest request) {
        FinishedProductResponse response = finishedProductService.updateFinishedProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/finished-products/{id}")
    @Operation(summary = "Delete finished product", description = "Deletes a finished product")
    public ResponseEntity<Map<String, String>> deleteFinishedProduct(@PathVariable Long id) {
        finishedProductService.deleteFinishedProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Finished product deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // === RAW PRODUCTS ===
    
    @PostMapping("/raw-materials")
    @Operation(summary = "Create raw material", description = "Creates a new raw material")
    public ResponseEntity<RawProductResponse> createRawProduct(@Valid @RequestBody RawProductRequest request) {
        RawProductResponse response = rawProductService.createRawProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/raw-materials")
    @Operation(summary = "Get all raw materials", description = "Retrieves all raw materials")
    public ResponseEntity<List<RawProductResponse>> getAllRawProducts() {
        List<RawProductResponse> response = rawProductService.getAllRawProducts();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/raw-materials/{id}")
    @Operation(summary = "Get raw material by ID", description = "Retrieves a raw material by its ID")
    public ResponseEntity<RawProductResponse> getRawProductById(@PathVariable Long id) {
        RawProductResponse response = rawProductService.getRawProductById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/raw-materials/{id}")
    @Operation(summary = "Update raw material", description = "Updates an existing raw material")
    public ResponseEntity<RawProductResponse> updateRawProduct(@PathVariable Long id, @Valid @RequestBody RawProductRequest request) {
        RawProductResponse response = rawProductService.updateRawProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/raw-materials/{id}")
    @Operation(summary = "Delete raw material", description = "Deletes a raw material")
    public ResponseEntity<Map<String, String>> deleteRawProduct(@PathVariable Long id) {
        rawProductService.deleteRawProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Raw product deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // === MACHINE PARTS ===
    
    @PostMapping("/machine-parts")
    @Operation(summary = "Create machine part", description = "Creates a new machine part")
    public ResponseEntity<MachinePartResponse> createMachinePart(@Valid @RequestBody MachinePartRequest request) {
        MachinePartResponse response = machinePartService.createMachinePart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/machine-parts")
    @Operation(summary = "Get all machine parts", description = "Retrieves all machine parts")
    public ResponseEntity<List<MachinePartResponse>> getAllMachineParts() {
        List<MachinePartResponse> response = machinePartService.getAllMachineParts();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/machine-parts/{id}")
    @Operation(summary = "Get machine part by ID", description = "Retrieves a machine part by its ID")
    public ResponseEntity<MachinePartResponse> getMachinePartById(@PathVariable Long id) {
        MachinePartResponse response = machinePartService.getMachinePartById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/machine-parts/{id}")
    @Operation(summary = "Update machine part", description = "Updates an existing machine part")
    public ResponseEntity<MachinePartResponse> updateMachinePart(@PathVariable Long id, @Valid @RequestBody MachinePartRequest request) {
        MachinePartResponse response = machinePartService.updateMachinePart(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/machine-parts/{id}")
    @Operation(summary = "Delete machine part", description = "Deletes a machine part")
    public ResponseEntity<Map<String, String>> deleteMachinePart(@PathVariable Long id) {
        machinePartService.deleteMachinePart(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Machine part deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // === STOCK OPERATIONS ===
    
    @PostMapping("/finished-products/{id}/increase-stock")
    @Operation(summary = "Increase finished product stock", description = "Increases stock for a finished product")
    public ResponseEntity<FinishedProductResponse> increaseFinishedProductStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        FinishedProductResponse response = finishedProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/finished-products/{id}/decrease-stock")
    @Operation(summary = "Decrease finished product stock", description = "Decreases stock for a finished product")
    public ResponseEntity<FinishedProductResponse> decreaseFinishedProductStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        FinishedProductResponse response = finishedProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/raw-materials/{id}/increase-stock")
    @Operation(summary = "Increase raw material stock", description = "Increases stock for a raw material")
    public ResponseEntity<RawProductResponse> increaseRawProductStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        RawProductResponse response = rawProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/raw-materials/{id}/decrease-stock")
    @Operation(summary = "Decrease raw material stock", description = "Decreases stock for a raw material")
    public ResponseEntity<RawProductResponse> decreaseRawProductStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        RawProductResponse response = rawProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    // === UTILITY ENDPOINTS ===
    
    @GetMapping("/finished-products/low-stock")
    @Operation(summary = "Get low stock finished products", description = "Retrieves finished products with low stock")
    public ResponseEntity<List<FinishedProductResponse>> getLowStockFinishedProducts() {
        List<FinishedProductResponse> response = finishedProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/raw-materials/low-stock")
    @Operation(summary = "Get low stock raw materials", description = "Retrieves raw materials with low stock")
    public ResponseEntity<List<RawProductResponse>> getLowStockRawProducts() {
        List<RawProductResponse> response = rawProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/machine-parts/category/{category}")
    @Operation(summary = "Get machine parts by category", description = "Retrieves machine parts by category")
    public ResponseEntity<List<MachinePartResponse>> getMachinePartsByCategory(@PathVariable MachinePart.Category category) {
        List<MachinePartResponse> response = machinePartService.getMachinePartsByCategory(category);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/machine-parts/condition/{condition}")
    @Operation(summary = "Get machine parts by condition", description = "Retrieves machine parts by condition")
    public ResponseEntity<List<MachinePartResponse>> getMachinePartsByCondition(@PathVariable MachinePart.Condition condition) {
        List<MachinePartResponse> response = machinePartService.getMachinePartsByCondition(condition);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/machine-parts/{id}/quantity")
    @Operation(summary = "Update machine part quantity", description = "Updates the quantity of a machine part")
    public ResponseEntity<MachinePartResponse> updateMachinePartQuantity(@PathVariable Long id, @RequestBody @Valid QuantityUpdateRequest request) {
        MachinePartResponse response = machinePartService.updateQuantity(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/machine-parts/{id}/condition")
    @Operation(summary = "Update machine part condition", description = "Updates the condition of a machine part")
    public ResponseEntity<MachinePartResponse> updateMachinePartCondition(@PathVariable Long id, @RequestBody @Valid ConditionUpdateRequest request) {
        MachinePartResponse response = machinePartService.updateCondition(id, request.getCondition());
        return ResponseEntity.ok(response);
    }
    
    // === INNER CLASSES ===
    
    public static class QuantityUpdateRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be non-negative")
        private Integer quantity;
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
    
    public static class ConditionUpdateRequest {
        @NotNull(message = "Condition is required")
        private MachinePart.Condition condition;
        
        public MachinePart.Condition getCondition() { return condition; }
        public void setCondition(MachinePart.Condition condition) { this.condition = condition; }
    }
}