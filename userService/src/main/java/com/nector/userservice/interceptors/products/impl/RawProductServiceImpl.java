package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.RawProductNotFoundException;
import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.RawProductResponse;
import com.nector.userservice.repository.RawProductRepository;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.model.RawProduct;
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
public class RawProductServiceImpl implements RawProductService {
    
    private final RawProductRepository rawProductRepository;
    
    @Override
    @Transactional
    public RawProductResponse createRawProduct(RawProductRequest request) {
        log.info("Creating raw product with material code: {}", request.getMaterialCode());
        
        if (rawProductRepository.existsByMaterialCode(request.getMaterialCode())) {
            throw new DataIntegrityViolationException("Raw product with material code " + request.getMaterialCode() + " already exists");
        }
        
        RawProduct product = new RawProduct();
        product.setName(request.getName());
        product.setMaterialCode(request.getMaterialCode());
        product.setUnit(request.getUnit());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        
        RawProduct savedProduct = rawProductRepository.save(product);
        log.info("Raw product created successfully with ID: {}", savedProduct.getId());
        
        return mapToResponse(savedProduct);
    }
    
    @Override
    @Transactional
    public RawProductResponse updateRawProduct(Long id, RawProductRequest request) {
        log.info("Updating raw product with ID: {}", id);
        
        RawProduct product = rawProductRepository.findById(id)
            .orElseThrow(() -> new RawProductNotFoundException(id));
        
        product.setName(request.getName());
        product.setUnit(request.getUnit());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        
        RawProduct updatedProduct = rawProductRepository.save(product);
        log.info("Raw product updated successfully with ID: {}", updatedProduct.getId());
        
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteRawProduct(Long id) {
        log.info("Soft deleting raw product with ID: {}", id);
        
        RawProduct product = rawProductRepository.findById(id)
            .orElseThrow(() -> new RawProductNotFoundException(id));
        
        product.setActive(false);
        rawProductRepository.save(product);
        
        log.info("Raw product soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RawProductResponse getRawProductById(Long id) {
        log.info("Fetching raw product with ID: {}", id);
        
        RawProduct product = rawProductRepository.findById(id)
            .orElseThrow(() -> new RawProductNotFoundException(id));
        
        return mapToResponse(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RawProductResponse> getAllRawProducts() {
        log.info("Fetching all raw products");
        
        return rawProductRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RawProductResponse increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for raw product ID: {} by quantity: {}", id, quantity);
        
        RawProduct product = rawProductRepository.findActiveById(id)
            .orElseThrow(() -> new RawProductNotFoundException(id));
        
        product.setQuantity(product.getQuantity() + quantity);
        RawProduct updatedProduct = rawProductRepository.save(product);
        
        log.info("Stock increased successfully for raw product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public RawProductResponse decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for raw product ID: {} by quantity: {}", id, quantity);
        
        RawProduct product = rawProductRepository.findActiveById(id)
            .orElseThrow(() -> new RawProductNotFoundException(id));
        
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getMaterialCode(), quantity, product.getQuantity());
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        RawProduct updatedProduct = rawProductRepository.save(product);
        
        if (updatedProduct.getQuantity() <= updatedProduct.getMinimumThreshold()) {
            log.warn("ALERT: Raw product {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}", 
                updatedProduct.getMaterialCode(), id, updatedProduct.getQuantity(), updatedProduct.getMinimumThreshold());
        }
        
        log.info("Stock decreased successfully for raw product ID: {}", id);
        return mapToResponse(updatedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RawProductResponse> getLowStockItems() {
        log.info("Fetching low stock raw products");
        
        return rawProductRepository.findLowStockItems().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private RawProductResponse mapToResponse(RawProduct product) {
        RawProductResponse response = new RawProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setMaterialCode(product.getMaterialCode());
        response.setUnit(product.getUnit());
        response.setQuantity(product.getQuantity());
        response.setMinimumThreshold(product.getMinimumThreshold());
        response.setActive(product.getActive());
        response.setLowStock(product.getQuantity() <= product.getMinimumThreshold());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}