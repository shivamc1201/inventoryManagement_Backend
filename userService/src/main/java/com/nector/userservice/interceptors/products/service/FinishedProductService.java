package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.FinishedProductRequest;
import com.nector.userservice.interceptors.products.model.FinishedProductResponse;

import java.util.List;

public interface FinishedProductService {
    
    FinishedProductResponse createFinishedProduct(FinishedProductRequest request);
    
    FinishedProductResponse updateFinishedProduct(Long id, FinishedProductRequest request);
    
    void deleteFinishedProduct(Long id);
    
    FinishedProductResponse getFinishedProductById(Long id);
    
    List<FinishedProductResponse> getAllFinishedProducts();
    
    FinishedProductResponse increaseStock(Long id, Integer quantity);
    
    FinishedProductResponse decreaseStock(Long id, Integer quantity);
    
    List<FinishedProductResponse> getLowStockItems();
}