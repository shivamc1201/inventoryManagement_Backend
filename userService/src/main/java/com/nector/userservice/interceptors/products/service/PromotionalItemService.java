package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.PromotionalItemResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionalItemService {

    PromotionalItemResponse createPromotionalItem(PromotionalItemRequest request);

    PromotionalItemResponse updatePromotionalItem(Long id, PromotionalItemRequest request);

    void deletePromotionalItem(Long id);

    PromotionalItemResponse getPromotionalItemById(Long id);

    List<PromotionalItemResponse> getAllPromotionalItems();

    PromotionalItemResponse increaseStock(Long id, BigDecimal quantity);

    PromotionalItemResponse decreaseStock(Long id, BigDecimal quantity);

    List<PromotionalItemResponse> getLowStockItems();

    PromotionalItemResponse updateByItemCode(String itemCode, PromotionalItemRequest request);
}
