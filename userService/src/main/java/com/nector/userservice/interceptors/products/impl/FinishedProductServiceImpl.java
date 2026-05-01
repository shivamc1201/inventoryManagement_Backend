package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.FinishedProductNotFoundException;
import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.interceptors.products.model.FinishedProductRequest;
import com.nector.userservice.interceptors.products.model.FinishedProductResponse;
import com.nector.userservice.interceptors.products.service.FinishedProductService;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.cloudinary.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinishedProductServiceImpl implements FinishedProductService {
    
    private final FinishedProductRepository finishedProductRepository;
    private final CloudinaryService cloudinaryService;
    
    @Override
    @Transactional
    @Operation(summary = "Create finished product", description = "Creates a new finished product")
    public FinishedProductResponse createFinishedProduct(FinishedProductRequest request, MultipartFile image) {
        log.info("Creating finished product with SKU: {}", request.getSku());
        
        // Null-safe check for SKU
        if (request.getSku() != null && finishedProductRepository.existsBySku(request.getSku())) {
            throw new DataIntegrityViolationException("Finished product with SKU " + request.getSku() + " already exists");
        }
        
        FinishedProduct product = new FinishedProduct();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setUnit(request.getUnit());
        product.setWeight(request.getWeight());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        product.setHsn(request.getHsn());
        product.setTaxRate(request.getTaxRate());
        product.setActive(true);
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            log.info("Uploading image for finished product with SKU: {}", request.getSku());
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(imageUrl);
            log.info("Image uploaded successfully for finished product with SKU: {}", request.getSku());
        }
        
        FinishedProduct savedProduct = finishedProductRepository.save(product);
        log.info("Finished product created successfully with ID: {}", savedProduct.getId());
        
        return mapToResponse(savedProduct);
    }
    
    @Override
    @Transactional
    @Operation(summary = "Update finished product", description = "Updates an existing finished product")
    public FinishedProductResponse updateFinishedProduct(Long id, FinishedProductRequest request, MultipartFile image) {
        log.info("Updating finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        // Only update fields if they are provided (not null)
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) product.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getHsn() != null) product.setHsn(request.getHsn());
        if (request.getTaxRate() != null) product.setTaxRate(request.getTaxRate());
        
        // Update status if provided in request
        if (request.getActive() != null) {
            product.setActive(request.getActive());
            String statusAction = request.getActive() ? "activated" : "deactivated";
            log.info("Product status updated to {} for finished product ID: {}", statusAction, id);
        }

        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            log.info("Uploading new image for finished product with ID: {}", id);
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(imageUrl);
            log.info("New image uploaded successfully for finished product with ID: {}", id);
        }
        
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        log.info("Finished product updated successfully with ID: {}", updatedProduct.getId());
        
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteFinishedProduct(Long id) {
        log.info("Soft deleting finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        product.setActive(false);
        finishedProductRepository.save(product);
        
        log.info("Finished product soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FinishedProductResponse getFinishedProductById(Long id) {
        log.info("Fetching finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        return mapToResponse(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FinishedProductResponse> getAllActiveFinishedProducts() {
        log.info("Fetching all finished products");
        
        return finishedProductRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public FinishedProductResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for finished product ID: {} by quantity: {}", id, quantity);
        
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be a non-negative number");
        }
        
        FinishedProduct product = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
        product.setQuantity(currentQty + quantity);
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        
        log.info("Stock increased successfully for finished product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public FinishedProductResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for finished product ID: {} by quantity: {}", id, quantity);
        
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be a non-negative number");
        }
        
        FinishedProduct product = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
        if (currentQty < quantity) {
            throw new InsufficientStockException(product.getSku(), quantity, currentQty);
        }
        
        product.setQuantity(currentQty - quantity);
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        
        if (updatedProduct.getQuantity() <= (updatedProduct.getMinimumThreshold() != null ? updatedProduct.getMinimumThreshold() : 0)) {
            log.warn("ALERT: Finished product {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}", 
                updatedProduct.getSku(), id, updatedProduct.getQuantity(), updatedProduct.getMinimumThreshold());
        }
        
        log.info("Stock decreased successfully for finished product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FinishedProductResponse> getLowStockItems() {
        log.info("Fetching low stock finished products");
        
        return finishedProductRepository.findLowStockItems().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private FinishedProductResponse mapToResponse(FinishedProduct product) {
        FinishedProductResponse response = new FinishedProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setUnit(product.getUnit());
        response.setWeight(product.getWeight());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        
        // Calculate per piece rate (price / quantity)
        BigDecimal perPieceRate = BigDecimal.ZERO;
        if (product.getQuantity() != null && product.getQuantity() > 0 && product.getPrice() != null) {
            perPieceRate = product.getPrice()
                .divide(BigDecimal.valueOf(product.getQuantity()), 2, RoundingMode.HALF_UP);
        }
        response.setPerPieceRate(perPieceRate);
        
        response.setMinimumThreshold(product.getMinimumThreshold());
        response.setHsn(product.getHsn());
        response.setTaxRate(product.getTaxRate());
        response.setActive(product.getActive());
        response.setImageUrl(product.getImageUrl());
        
        Integer threshold = product.getMinimumThreshold() != null ? product.getMinimumThreshold() : 0;
        int qty = product.getQuantity() != null ? product.getQuantity() : 0;
        response.setLowStock(qty <= threshold);
        
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setStatus(product.getStatus());
        return response;
    }

    @Override
    public FinishedProductResponse updateProductStatus(Long id, Boolean status) {
        FinishedProduct product = finishedProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Finished product not found: " + id));

        product.setActive(status);
        FinishedProduct updatedProduct = finishedProductRepository.save(product);

        String action = status ? "activated" : "suspended";
        log.info("Finished product {}: {}", action, updatedProduct.getSku());
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinishedProductResponse> getAllFinishedProductsWithoutStatusCheck() {
        log.info("Fetching all finished products (including inactive)");

        return finishedProductRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FinishedProductResponse updateBySku(String sku, FinishedProductRequest request, MultipartFile image) {
        log.info("Updating finished product by SKU: {}", sku);

        FinishedProduct product = finishedProductRepository.findBySku(sku)
                .orElseThrow(() -> new FinishedProductNotFoundException(0L));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) product.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getHsn() != null) product.setHsn(request.getHsn());
        if (request.getTaxRate() != null) product.setTaxRate(request.getTaxRate());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(imageUrl);
        }

        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        log.info("Finished product updated by SKU: {}", sku);
        return mapToResponse(updatedProduct);
    }
}
