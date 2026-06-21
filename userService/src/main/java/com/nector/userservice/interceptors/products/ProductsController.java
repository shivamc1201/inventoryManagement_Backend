package com.nector.userservice.interceptors.products;

import com.nector.userservice.dto.inventory.StockUpdateRequest;
import com.nector.userservice.interceptors.products.impl.UpdateMachinePart;
import com.nector.userservice.interceptors.products.model.*;
import com.nector.userservice.interceptors.products.service.FinishedProductService;
import com.nector.userservice.interceptors.products.service.MachinePartService;
import com.nector.userservice.interceptors.products.service.ProductInwardApprovalService;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.interceptors.products.service.PromotionalItemService;
import com.nector.userservice.interceptors.products.service.ScrapItemService;
import com.nector.userservice.interceptors.products.service.OutwardItemService;
import com.nector.userservice.model.MachinePart;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.model.ProductInwardApproval;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "APIs for product management including finished products, raw materials, machine parts, promotional items, and scrap items")
public class ProductsController {

    private final FinishedProductService finishedProductService;
    private final RawProductService rawProductService;
    private final MachinePartService machinePartService;
    private final PromotionalItemService promotionalItemService;
    private final ScrapItemService scrapItemService;
    private final OutwardItemService outwardItemService;
    private final ProductInwardApprovalService productInwardApprovalService;

    // ==================== FINISHED PRODUCTS ====================

    // JSON endpoint - Swagger and web without image
    @PostMapping(value = "/finished-products", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create finished product (JSON)", description = "Creates a new finished product with JSON payload. Use this for requests without image.")
    public ResponseEntity<FinishedProductResponse> createFinishedProductJson(@RequestBody FinishedProductRequest request) {
        FinishedProductResponse response = finishedProductService.createFinishedProduct(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Multipart endpoint - Web with optional image
    @PostMapping(value = "/finished-products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create finished product (Multipart)", description = "Creates a new finished product with optional image. Use this for requests with image upload.")
    public ResponseEntity<FinishedProductResponse> createFinishedProductMultipart(
            @Parameter(description = "Finished product request JSON",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") FinishedProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        FinishedProductResponse response = finishedProductService.createFinishedProduct(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/finished-products")
    @Operation(summary = "Get all finished products", description = "Retrieves all active finished products")
    public ResponseEntity<List<FinishedProductResponse>> getAllFinishedProducts() {
        List<FinishedProductResponse> response = finishedProductService.getAllActiveFinishedProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finished-products/all")
    @Operation(summary = "Get all finished products (including inactive)", description = "Retrieves all finished products regardless of active status")
    public ResponseEntity<List<FinishedProductResponse>> getAllFinishedProductsWithoutStatusCheck() {
        List<FinishedProductResponse> response = finishedProductService.getAllFinishedProductsWithoutStatusCheck();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finished-products/{id}")
    @Operation(summary = "Get finished product by ID", description = "Retrieves a finished product by its ID")
    public ResponseEntity<FinishedProductResponse> getFinishedProductById(@PathVariable Long id) {
        FinishedProductResponse response = finishedProductService.getFinishedProductById(id);
        return ResponseEntity.ok(response);
    }

    // JSON endpoint - Swagger and web without image
    @PutMapping(value = "/finished-products/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update finished product (JSON)", description = "Updates a finished product with JSON payload. Use this for requests without image.")
    public ResponseEntity<FinishedProductResponse> updateFinishedProductJson(@PathVariable Long id, @RequestBody FinishedProductRequest request) {
        FinishedProductResponse response = finishedProductService.updateFinishedProduct(id, request, null);
        return ResponseEntity.ok(response);
    }

    // Multipart endpoint - Web with optional image
    @PutMapping(value = "/finished-products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update finished product (Multipart)", description = "Updates a finished product with optional image. Use this for requests with image upload.")
    public ResponseEntity<FinishedProductResponse> updateFinishedProductMultipart(
            @PathVariable Long id,
            @Parameter(description = "Finished product request JSON",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") FinishedProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        FinishedProductResponse response = finishedProductService.updateFinishedProduct(id, request, image);
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

    @PostMapping("/finished-products/{id}/increase-stock")
    @Operation(summary = "Increase finished product stock", description = "Increases stock for a finished product")
    public ResponseEntity<FinishedProductResponse> increaseFinishedProductStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        FinishedProductResponse response = finishedProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finished-products/{id}/decrease-stock")
    @Operation(summary = "Decrease finished product stock", description = "Decreases stock for a finished product")
    public ResponseEntity<FinishedProductResponse> decreaseFinishedProductStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        FinishedProductResponse response = finishedProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/finished-products/low-stock")
    @Operation(summary = "Get low stock finished products", description = "Retrieves finished products with low stock")
    public ResponseEntity<List<FinishedProductResponse>> getLowStockFinishedProducts() {
        List<FinishedProductResponse> response = finishedProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }

    // ==================== RAW MATERIALS ====================

    // JSON endpoint - Swagger and web without image
    @PostMapping(value = "/raw-materials", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create raw material (JSON)", description = "Creates a new raw material with JSON payload. Use this for requests without image.")
    public ResponseEntity<RawProductResponse> createRawProduct(@RequestBody RawProductRequest request) {
        RawProductResponse response = rawProductService.createRawProduct(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Multipart endpoint - Web with optional image
    @PostMapping(value = "/raw-materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create raw material (Multipart)", description = "Creates a new raw material with optional image. Use this for requests with image upload.")
    public ResponseEntity<RawProductResponse> createRawProductMultipart(
            @Parameter(description = "Raw product request JSON",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") RawProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        RawProductResponse response = rawProductService.createRawProduct(request, image);
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

    @GetMapping("/raw-materials/{id}/image")
    @Operation(summary = "Get raw material image by ID", description = "Redirects to the image of a raw material by its ID")
    public ResponseEntity<Void> getRawProductImage(@PathVariable Long id) {
        String imageUrl = rawProductService.getRawProductImageUrl(id);
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create(imageUrl))
                .build();
    }

    @GetMapping("/raw-materials/material-code/{materialCode}/image")
    @Operation(summary = "Get raw material image by material code", description = "Redirects to the image of a raw material by its material code")
    public ResponseEntity<Void> getRawProductImageByMaterialCode(@PathVariable String materialCode) {
        String imageUrl = rawProductService.getRawProductImageUrlByMaterialCode(materialCode);
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create(imageUrl))
                .build();
    }

    // JSON endpoint - Swagger and web without image
    @PutMapping(value = "/raw-materials/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update raw material (JSON)", description = "Updates an existing raw material with JSON payload. Blocked if a pending inward approval exists for this product.")
    public ResponseEntity<RawProductResponse> updateRawProduct(@PathVariable Long id, @RequestBody RawProductRequest request) {
        if (productInwardApprovalService.hasPendingApproval(ProductInwardApproval.ProductType.RAW_PRODUCT, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for raw material ID: " + id);
        }
        RawProductResponse response = rawProductService.updateRawProduct(id, request, null);
        return ResponseEntity.ok(response);
    }

    // Multipart endpoint - Web with optional image
    @PutMapping(value = "/raw-materials/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update raw material (Multipart)", description = "Updates an existing raw material with optional image. Blocked if a pending inward approval exists.")
    public ResponseEntity<RawProductResponse> updateRawProductMultipart(
            @PathVariable Long id,
            @Parameter(description = "Raw product request JSON",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") RawProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        if (productInwardApprovalService.hasPendingApproval(ProductInwardApproval.ProductType.RAW_PRODUCT, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for raw material ID: " + id);
        }
        RawProductResponse response = rawProductService.updateRawProduct(id, request, image);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/raw-materials/{id}")
    @Operation(summary = "Submit raw material inward for approval (PATCH)", description = "Submits an inward stock entry for a raw material. Requires admin approval before the stock is updated.")
    public ResponseEntity<Map<String, Object>> patchRawProduct(@PathVariable Long id, @RequestBody RawProductRequest request) {
        Map<String, Object> response = productInwardApprovalService.createRawProductInwardApproval(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/raw-materials/{id}")
    @Operation(summary = "Delete raw material", description = "Deletes a raw material")
    public ResponseEntity<Map<String, String>> deleteRawProduct(@PathVariable Long id) {
        rawProductService.deleteRawProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Raw product deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/raw-materials/{id}/increase-stock")
    @Operation(summary = "Increase raw material stock", description = "Increases stock for a raw material")
    public ResponseEntity<RawProductResponse> increaseRawProductStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        RawProductResponse response = rawProductService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/raw-materials/{id}/decrease-stock")
    @Operation(summary = "Decrease raw material stock", description = "Decreases stock for a raw material")
    public ResponseEntity<RawProductResponse> decreaseRawProductStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        RawProductResponse response = rawProductService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/raw-materials/low-stock")
    @Operation(summary = "Get low stock raw materials", description = "Retrieves raw materials with low stock")
    public ResponseEntity<List<RawProductResponse>> getLowStockRawProducts() {
        List<RawProductResponse> response = rawProductService.getLowStockItems();
        return ResponseEntity.ok(response);
    }

    // ==================== MACHINE PARTS ====================

    @PostMapping("/machine-parts")
    @Operation(summary = "Create machine part", description = "Creates a new machine part")
    public ResponseEntity<MachinePartResponse> createMachinePart(@RequestBody MachinePartRequest request) {
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
    @Operation(summary = "Update machine part", description = "Updates an existing machine part. Blocked if a pending inward approval exists for this product.")
    public ResponseEntity<MachinePartResponse> updateMachinePart(@PathVariable Long id, @RequestBody UpdateMachinePart request) {
        if (productInwardApprovalService.hasPendingApproval(ProductInwardApproval.ProductType.MACHINE_PART, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for machine part ID: " + id);
        }
        MachinePartResponse response = machinePartService.updateMachinePart(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/machine-parts/{id}")
    @Operation(summary = "Submit machine part inward for approval (PATCH)", description = "Submits an inward stock entry for a machine part. Requires admin approval before the stock is updated.")
    public ResponseEntity<Map<String, Object>> patchMachinePart(@PathVariable Long id, @RequestBody UpdateMachinePart request) {
        Map<String, Object> response = productInwardApprovalService.createMachinePartInwardApproval(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/machine-parts/{id}")
    @Operation(summary = "Delete machine part", description = "Deletes a machine part")
    public ResponseEntity<Map<String, String>> deleteMachinePart(@PathVariable Long id) {
        machinePartService.deleteMachinePart(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Machine part deleted successfully");
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
    public ResponseEntity<MachinePartResponse> updateMachinePartQuantity(@PathVariable Long id, @RequestBody QuantityUpdateRequest request) {
        MachinePartResponse response = machinePartService.updateQuantity(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/machine-parts/{id}/condition")
    @Operation(summary = "Update machine part condition", description = "Updates the condition of a machine part")
    public ResponseEntity<MachinePartResponse> updateMachinePartCondition(@PathVariable Long id, @RequestBody ConditionUpdateRequest request) {
        MachinePartResponse response = machinePartService.updateCondition(id, request.getCondition());
        return ResponseEntity.ok(response);
    }

    // ==================== PROMOTIONAL ITEMS ====================

    @PostMapping("/promotional-items")
    @Operation(summary = "Create promotional item", description = "Creates a new promotional item")
    public ResponseEntity<PromotionalItemResponse> createPromotionalItem(@RequestBody PromotionalItemRequest request) {
        PromotionalItemResponse response = promotionalItemService.createPromotionalItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/promotional-items")
    @Operation(summary = "Get all promotional items", description = "Retrieves all promotional items")
    public ResponseEntity<List<PromotionalItemResponse>> getAllPromotionalItems() {
        List<PromotionalItemResponse> response = promotionalItemService.getAllPromotionalItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotional-items/{id}")
    @Operation(summary = "Get promotional item by ID", description = "Retrieves a promotional item by its ID")
    public ResponseEntity<PromotionalItemResponse> getPromotionalItemById(@PathVariable Long id) {
        PromotionalItemResponse response = promotionalItemService.getPromotionalItemById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/promotional-items/{id}")
    @Operation(summary = "Update promotional item", description = "Updates an existing promotional item. Blocked if a pending inward approval exists for this product.")
    public ResponseEntity<PromotionalItemResponse> updatePromotionalItem(@PathVariable Long id, @RequestBody PromotionalItemRequest request) {
        if (productInwardApprovalService.hasPendingApproval(ProductInwardApproval.ProductType.PROMOTIONAL_ITEM, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for promotional item ID: " + id);
        }
        PromotionalItemResponse response = promotionalItemService.updatePromotionalItem(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/promotional-items/{id}")
    @Operation(summary = "Submit promotional item inward for approval (PATCH)", description = "Submits an inward stock entry for a promotional item. Requires admin approval before the stock is updated.")
    public ResponseEntity<Map<String, Object>> patchPromotionalItem(@PathVariable Long id, @RequestBody PromotionalItemRequest request) {
        Map<String, Object> response = productInwardApprovalService.createPromotionalItemInwardApproval(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/promotional-items/{id}")
    @Operation(summary = "Delete promotional item", description = "Deletes a promotional item")
    public ResponseEntity<Map<String, String>> deletePromotionalItem(@PathVariable Long id) {
        promotionalItemService.deletePromotionalItem(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotional item deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promotional-items/{id}/increase-stock")
    @Operation(summary = "Increase promotional item stock", description = "Increases stock for a promotional item")
    public ResponseEntity<PromotionalItemResponse> increasePromotionalItemStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        PromotionalItemResponse response = promotionalItemService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promotional-items/{id}/decrease-stock")
    @Operation(summary = "Decrease promotional item stock", description = "Decreases stock for a promotional item")
    public ResponseEntity<PromotionalItemResponse> decreasePromotionalItemStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        PromotionalItemResponse response = promotionalItemService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotional-items/low-stock")
    @Operation(summary = "Get low stock promotional items", description = "Retrieves promotional items with low stock")
    public ResponseEntity<List<PromotionalItemResponse>> getLowStockPromotionalItems() {
        List<PromotionalItemResponse> response = promotionalItemService.getLowStockItems();
        return ResponseEntity.ok(response);
    }

    // ==================== SCRAP ITEMS ====================

    @PostMapping("/scrap-items")
    @Operation(summary = "Create scrap item", description = "Creates a new scrap item")
    public ResponseEntity<ScrapItemResponse> createScrapItem(@RequestBody ScrapItemRequest request) {
        ScrapItemResponse response = scrapItemService.createScrapItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/scrap-items")
    @Operation(summary = "Get all scrap items", description = "Retrieves all scrap items")
    public ResponseEntity<List<ScrapItemResponse>> getAllScrapItems() {
        List<ScrapItemResponse> response = scrapItemService.getAllScrapItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scrap-items/{id}")
    @Operation(summary = "Get scrap item by ID", description = "Retrieves a scrap item by its ID")
    public ResponseEntity<ScrapItemResponse> getScrapItemById(@PathVariable Long id) {
        ScrapItemResponse response = scrapItemService.getScrapItemById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/scrap-items/{id}")
    @Operation(summary = "Update scrap item", description = "Updates an existing scrap item. Blocked if a pending inward approval exists for this product.")
    public ResponseEntity<ScrapItemResponse> updateScrapItem(@PathVariable Long id, @RequestBody ScrapItemRequest request) {
        if (productInwardApprovalService.hasPendingApproval(ProductInwardApproval.ProductType.SCRAP_ITEM, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for scrap item ID: " + id);
        }
        ScrapItemResponse response = scrapItemService.updateScrapItem(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/scrap-items/{id}")
    @Operation(summary = "Submit scrap item inward for approval (PATCH)", description = "Submits an inward stock entry for a scrap item. Requires admin approval before the stock is updated.")
    public ResponseEntity<Map<String, Object>> patchScrapItem(@PathVariable Long id, @RequestBody ScrapItemRequest request) {
        Map<String, Object> response = productInwardApprovalService.createScrapItemInwardApproval(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/scrap-items/{id}")
    @Operation(summary = "Delete scrap item", description = "Deletes a scrap item")
    public ResponseEntity<Map<String, String>> deleteScrapItem(@PathVariable Long id) {
        scrapItemService.deleteScrapItem(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Scrap item deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scrap-items/{id}/increase-stock")
    @Operation(summary = "Increase scrap item stock", description = "Increases stock for a scrap item")
    public ResponseEntity<ScrapItemResponse> increaseScrapItemStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        ScrapItemResponse response = scrapItemService.increaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scrap-items/{id}/decrease-stock")
    @Operation(summary = "Decrease scrap item stock", description = "Decreases stock for a scrap item")
    public ResponseEntity<ScrapItemResponse> decreaseScrapItemStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        ScrapItemResponse response = scrapItemService.decreaseStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scrap-items/low-stock")
    @Operation(summary = "Get low stock scrap items", description = "Retrieves scrap items with low stock")
    public ResponseEntity<List<ScrapItemResponse>> getLowStockScrapItems() {
        List<ScrapItemResponse> response = scrapItemService.getLowStockItems();
        return ResponseEntity.ok(response);
    }

    // ==================== OUTWARD ITEMS ====================

    @PostMapping("/outward-items")
    @Operation(summary = "Create outward item transaction", description = "Creates a new outward item transaction for spare parts, promotional items, or scrap material. Transaction type can be OUTWARD_GIVING or RETURNED_PART.")
    public ResponseEntity<OutwardItemResponse> createOutwardItem(@RequestBody OutwardItemRequest request) {
        OutwardItemResponse response = outwardItemService.createOutwardItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/outward-items")
    @Operation(summary = "Get all outward item transactions", description = "Retrieves all outward item transactions")
    public ResponseEntity<List<OutwardItemResponse>> getAllOutwardItems() {
        List<OutwardItemResponse> response = outwardItemService.getAllOutwardItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/{id}")
    @Operation(summary = "Get outward item transaction by ID", description = "Retrieves an outward item transaction by its ID")
    public ResponseEntity<OutwardItemResponse> getOutwardItemById(@PathVariable Long id) {
        OutwardItemResponse response = outwardItemService.getOutwardItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/type/{itemType}")
    @Operation(summary = "Get outward items by type", description = "Retrieves outward item transactions by item type (SPARE_PARTS, PROMOTIONAL_ITEMS, SCRAP_MATERIAL)")
    public ResponseEntity<List<OutwardItemResponse>> getOutwardItemsByType(@PathVariable OutwardItemTransaction.ItemType itemType) {
        List<OutwardItemResponse> response = outwardItemService.getOutwardItemsByType(itemType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/transaction-type/{transactionType}")
    @Operation(summary = "Get outward items by transaction type", description = "Retrieves outward item transactions by transaction type (OUTWARD_GIVING, RETURNED_PART)")
    public ResponseEntity<List<OutwardItemResponse>> getOutwardItemsByTransactionType(@PathVariable OutwardItemTransaction.TransactionType transactionType) {
        List<OutwardItemResponse> response = outwardItemService.getOutwardItemsByTransactionType(transactionType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/returned-for-selling")
    @Operation(summary = "Get returned parts ready for selling", description = "Retrieves returned parts (RETURNED_PART transaction type) by item type. Use this to get scrap material or spare parts that are returned and ready to be sold. Query params: itemType (SCRAP_MATERIAL, SPARE_PARTS, PROMOTIONAL_ITEMS)")
    public ResponseEntity<List<OutwardItemResponse>> getReturnedPartsForSelling(
            @RequestParam OutwardItemTransaction.ItemType itemType) {
        List<OutwardItemResponse> response = outwardItemService.getOutwardItemsByTypeAndTransaction(
                itemType, OutwardItemTransaction.TransactionType.RETURNED_PART);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/filter")
    @Operation(summary = "Filter outward items", description = "Retrieves outward item transactions filtered by item type and/or transaction type using query parameters")
    public ResponseEntity<List<OutwardItemResponse>> getOutwardItemsByFilter(
            @RequestParam(required = false) OutwardItemTransaction.ItemType itemType,
            @RequestParam(required = false) OutwardItemTransaction.TransactionType transactionType) {
        List<OutwardItemResponse> response;
        if (itemType != null && transactionType != null) {
            response = outwardItemService.getOutwardItemsByTypeAndTransaction(itemType, transactionType);
        } else if (itemType != null) {
            response = outwardItemService.getOutwardItemsByType(itemType);
        } else if (transactionType != null) {
            response = outwardItemService.getOutwardItemsByTransactionType(transactionType);
        } else {
            response = outwardItemService.getAllOutwardItems();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/outward-items/scrap")
    @Operation(summary = "Get scrap outward items", description = "Retrieves outward item transactions with item type SPARE_PARTS")
    public ResponseEntity<List<OutwardItemResponse>> getScrapOutwardItems() {
        List<OutwardItemResponse> response = outwardItemService.getOutwardItemsByType(OutwardItemTransaction.ItemType.SPARE_PARTS);
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE BY SKU ====================

    @PutMapping(value = "/finished-products/sku/{sku}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update finished product by SKU (JSON)", description = "Updates a finished product using its SKU. Only provided fields are updated.")
    public ResponseEntity<FinishedProductResponse> updateFinishedProductBySku(@PathVariable String sku, @RequestBody FinishedProductRequest request) {
        FinishedProductResponse response = finishedProductService.updateBySku(sku, request, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/finished-products/sku/{sku}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update finished product by SKU (Multipart)", description = "Updates a finished product using its SKU with optional image.")
    public ResponseEntity<FinishedProductResponse> updateFinishedProductBySkuMultipart(
            @PathVariable String sku,
            @RequestPart("request") FinishedProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        FinishedProductResponse response = finishedProductService.updateBySku(sku, request, image);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/raw-materials/sku/{materialCode}")
    @Operation(summary = "Update raw material by material code", description = "Updates a raw material using its material code. Blocked if a pending inward approval exists.")
    public ResponseEntity<RawProductResponse> updateRawProductByMaterialCode(@PathVariable String materialCode, @RequestBody RawProductRequest request) {
        if (productInwardApprovalService.hasPendingApprovalByProductCode(ProductInwardApproval.ProductType.RAW_PRODUCT, materialCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for raw material: " + materialCode);
        }
        RawProductResponse response = rawProductService.updateByMaterialCode(materialCode, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/machine-parts/sku/{partNumber}")
    @Operation(summary = "Update machine part by part number", description = "Updates a machine part using its part number. Blocked if a pending inward approval exists.")
    public ResponseEntity<MachinePartResponse> updateMachinePartByPartNumber(@PathVariable String partNumber, @RequestBody MachinePartRequest request) {
        if (productInwardApprovalService.hasPendingApprovalByProductCode(ProductInwardApproval.ProductType.MACHINE_PART, partNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for machine part: " + partNumber);
        }
        MachinePartResponse response = machinePartService.updateByPartNumber(partNumber, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/promotional-items/sku/{itemCode}")
    @Operation(summary = "Update promotional item by item code", description = "Updates a promotional item using its item code. Blocked if a pending inward approval exists.")
    public ResponseEntity<PromotionalItemResponse> updatePromotionalItemByItemCode(@PathVariable String itemCode, @RequestBody PromotionalItemRequest request) {
        if (productInwardApprovalService.hasPendingApprovalByProductCode(ProductInwardApproval.ProductType.PROMOTIONAL_ITEM, itemCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for promotional item: " + itemCode);
        }
        PromotionalItemResponse response = promotionalItemService.updateByItemCode(itemCode, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/scrap-items/sku/{itemCode}")
    @Operation(summary = "Update scrap item by item code", description = "Updates a scrap item using its item code. Blocked if a pending inward approval exists.")
    public ResponseEntity<ScrapItemResponse> updateScrapItemByItemCode(@PathVariable String itemCode, @RequestBody ScrapItemRequest request) {
        if (productInwardApprovalService.hasPendingApprovalByProductCode(ProductInwardApproval.ProductType.SCRAP_ITEM, itemCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please approve the last inward entry first for scrap item: " + itemCode);
        }
        ScrapItemResponse response = scrapItemService.updateByItemCode(itemCode, request);
        return ResponseEntity.ok(response);
    }

    // ==================== INWARD APPROVAL (ADMIN) ====================

    @GetMapping("/inward-approvals/pending")
    @Operation(summary = "Get all pending inward approvals", description = "Retrieves all pending product inward approval requests across all product types")
    public ResponseEntity<List<ProductInwardApproval>> getPendingInwardApprovals() {
        List<ProductInwardApproval> approvals = productInwardApprovalService.getPendingApprovals();
        return ResponseEntity.ok(approvals);
    }

    @PostMapping("/inward-approvals/{approvalId}/process")
    @Operation(summary = "Process product inward approval", description = "Approves or rejects a product inward request. On approval, stock is updated in the database.")
    public ResponseEntity<Map<String, Object>> processInwardApproval(
            @PathVariable Long approvalId,
            @RequestBody InwardApprovalRequest request,
            @RequestHeader("Admin-Username") String adminUsername) {
        Map<String, Object> response = productInwardApprovalService.processApproval(
                approvalId, request.getAction(), adminUsername, request.getComments());
        return ResponseEntity.ok(response);
    }

    // ==================== INNER CLASSES ====================

    public static class QuantityUpdateRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class ConditionUpdateRequest {
        private MachinePart.Condition condition;

        public MachinePart.Condition getCondition() { return condition; }
        public void setCondition(MachinePart.Condition condition) { this.condition = condition; }
    }

    @Data
    public static class InwardApprovalRequest {
        private String action; // APPROVE or REJECT
        private String comments;
    }
}
