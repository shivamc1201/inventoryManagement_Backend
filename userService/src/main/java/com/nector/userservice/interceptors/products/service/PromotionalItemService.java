package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.PromotionalItemResponse;

import java.util.List;

public interface PromotionalItemService {
    
    PromotionalItemResponse createPromotionalItem(PromotionalItemRequest request);
    
    PromotionalItemResponse updatePromotionalItem(Long id, PromotionalItemRequest request);
    
    void deletePromotionalItem(Long id);
    
    PromotionalItemResponse getPromotionalItemById(Long id);
    
    List<PromotionalItemResponse> getAllPromotionalItems();
    
    PromotionalItemResponse increaseStock(Long id, Integer quantity);
    
    PromotionalItemResponse decreaseStock(Long id, Integer quantity);
    
    List<PromotionalItemResponse> getLowStockItems();
}
