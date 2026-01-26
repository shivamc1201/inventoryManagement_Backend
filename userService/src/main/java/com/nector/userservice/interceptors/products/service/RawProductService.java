package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.RawProductResponse;

import java.util.List;

public interface RawProductService {
    
    RawProductResponse createRawProduct(RawProductRequest request);
    
    RawProductResponse updateRawProduct(Long id, RawProductRequest request);
    
    void deleteRawProduct(Long id);
    
    RawProductResponse getRawProductById(Long id);
    
    List<RawProductResponse> getAllRawProducts();
    
    RawProductResponse increaseStock(Long id, Integer quantity);
    
    RawProductResponse decreaseStock(Long id, Integer quantity);
    
    List<RawProductResponse> getLowStockItems();
}