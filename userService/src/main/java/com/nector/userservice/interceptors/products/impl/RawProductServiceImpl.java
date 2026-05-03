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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        
        if (request.getMaterialCode() != null && rawProductRepository.existsByMaterialCode(request.getMaterialCode())) {
            throw new DataIntegrityViolationException("Raw product with material code " + request.getMaterialCode() + " already exists");
        }
        
        RawProduct product = new RawProduct();
        product.setName(request.getName());
        product.setMaterialCode(request.getMaterialCode());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        product.setVendorId(request.getVendorId());
        product.setVendorName(request.getVendorName());
        product.setTransportName(request.getTransportName());
        product.setDriverName(request.getDriverName());
        product.setDriverMobile(request.getDriverMobile());
        product.setActive(true);
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        
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
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        product.setHsn(request.getHsn());
        product.setTaxRate(request.getTaxRate());
        product.setVendorId(request.getVendorId());
        product.setVendorName(request.getVendorName());
        product.setTransportName(request.getTransportName());
        product.setDriverName(request.getDriverName());
        product.setDriverMobile(request.getDriverMobile());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        
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
        
        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
        product.setQuantity(currentQty + quantity);
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
        
        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
        if (currentQty < quantity) {
            throw new InsufficientStockException(product.getMaterialCode(), quantity, currentQty);
        }
        
        product.setQuantity(currentQty - quantity);
        RawProduct updatedProduct = rawProductRepository.save(product);
        
        int updatedQty = updatedProduct.getQuantity() != null ? updatedProduct.getQuantity() : 0;
        int threshold = updatedProduct.getMinimumThreshold() != null ? updatedProduct.getMinimumThreshold() : 0;
        if (updatedQty <= threshold) {
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
        response.setPrice(product.getPrice());
        
        // Set per item price same as price
        BigDecimal perItemPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        response.setPerItemPrice(perItemPrice);
        
        response.setMinimumThreshold(product.getMinimumThreshold());
        response.setHsn(product.getHsn());
        response.setTaxRate(product.getTaxRate());
        response.setActive(product.getActive());
        int qty = product.getQuantity() != null ? product.getQuantity() : 0;
        int minThreshold = product.getMinimumThreshold() != null ? product.getMinimumThreshold() : 0;
        response.setLowStock(qty <= minThreshold);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setVendorId(product.getVendorId());
        response.setVendorName(product.getVendorName());
        response.setTransportName(product.getTransportName());
        response.setDriverName(product.getDriverName());
        response.setDriverName(product.getDriverName());
        response.setDriverMobile(product.getDriverMobile());
        response.setStatus(product.getStatus());
        return response;
    }

    @Override
    @Transactional
    public RawProductResponse updateByMaterialCode(String materialCode, RawProductRequest request) {
        log.info("Updating raw product by material code: {}", materialCode);

        RawProduct product = rawProductRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new RawProductNotFoundException(0L));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) product.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getHsn() != null) product.setHsn(request.getHsn());
        if (request.getTaxRate() != null) product.setTaxRate(request.getTaxRate());
        if (request.getVendorId() != null) product.setVendorId(request.getVendorId());
        if (request.getVendorName() != null) product.setVendorName(request.getVendorName());
        if (request.getTransportName() != null) product.setTransportName(request.getTransportName());
        if (request.getDriverName() != null) product.setDriverName(request.getDriverName());
        if (request.getDriverMobile() != null) product.setDriverMobile(request.getDriverMobile());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        RawProduct updatedProduct = rawProductRepository.save(product);
        log.info("Raw product updated by material code: {}", materialCode);
        return mapToResponse(updatedProduct);
    }
}