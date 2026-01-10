package com.nector.userservice.service;

import com.nector.userservice.dto.finishedproduct.FinishedProductRequest;
import com.nector.userservice.dto.finishedproduct.FinishedProductResponse;
import com.nector.userservice.exception.FinishedProductNotFoundException;
import com.nector.userservice.exception.InsufficientStockException;
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
public class FinishedProductService {
    
    private final FinishedProductRepository finishedProductRepository;
    
    @Transactional
    public FinishedProductResponse createFinishedProduct(FinishedProductRequest request) {
        log.info("Creating finished product with SKU: {}", request.getSku());
        
        if (finishedProductRepository.existsBySku(request.getSku())) {
            throw new DataIntegrityViolationException("Finished product with SKU " + request.getSku() + " already exists");
        }
        
        FinishedProduct product = new FinishedProduct();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        
        FinishedProduct savedProduct = finishedProductRepository.save(product);
        log.info("Finished product created successfully with ID: {}", savedProduct.getId());
        
        return mapToResponse(savedProduct);
    }
    
    @Transactional
    public FinishedProductResponse updateFinishedProduct(Long id, FinishedProductRequest request) {
        log.info("Updating finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        log.info("Finished product updated successfully with ID: {}", updatedProduct.getId());
        
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteFinishedProduct(Long id) {
        log.info("Soft deleting finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        product.setActive(false);
        finishedProductRepository.save(product);
        
        log.info("Finished product soft deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public FinishedProductResponse getFinishedProductById(Long id) {
        log.info("Fetching finished product with ID: {}", id);
        
        FinishedProduct product = finishedProductRepository.findById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    public List<FinishedProductResponse> getAllFinishedProducts() {
        log.info("Fetching all finished products");
        
        return finishedProductRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public FinishedProductResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for finished product ID: {} by quantity: {}", id, quantity);
        
        FinishedProduct product = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        product.setQuantity(product.getQuantity() + quantity);
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        
        log.info("Stock increased successfully for finished product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    public FinishedProductResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for finished product ID: {} by quantity: {}", id, quantity);
        
        FinishedProduct product = finishedProductRepository.findActiveById(id)
            .orElseThrow(() -> new FinishedProductNotFoundException(id));
        
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getSku(), quantity, product.getQuantity());
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        FinishedProduct updatedProduct = finishedProductRepository.save(product);
        
        // Check if stock falls below threshold and log alert
        if (updatedProduct.getQuantity() <= updatedProduct.getMinimumThreshold()) {
            log.warn("ALERT: Finished product {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}", 
                updatedProduct.getSku(), id, updatedProduct.getQuantity(), updatedProduct.getMinimumThreshold());
        }
        
        log.info("Stock decreased successfully for finished product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
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
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setMinimumThreshold(product.getMinimumThreshold());
        response.setActive(product.getActive());
        response.setLowStock(product.getQuantity() <= product.getMinimumThreshold());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}