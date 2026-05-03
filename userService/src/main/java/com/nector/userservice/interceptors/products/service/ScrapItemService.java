package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.ScrapItemRequest;
import com.nector.userservice.interceptors.products.model.ScrapItemResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ScrapItemService {

    ScrapItemResponse createScrapItem(ScrapItemRequest request);

    ScrapItemResponse updateScrapItem(Long id, ScrapItemRequest request);

    void deleteScrapItem(Long id);

    ScrapItemResponse getScrapItemById(Long id);

    List<ScrapItemResponse> getAllScrapItems();

    ScrapItemResponse increaseStock(Long id, BigDecimal quantity);

    ScrapItemResponse decreaseStock(Long id, BigDecimal quantity);

    List<ScrapItemResponse> getLowStockItems();

    ScrapItemResponse updateByItemCode(String itemCode, ScrapItemRequest request);
}
