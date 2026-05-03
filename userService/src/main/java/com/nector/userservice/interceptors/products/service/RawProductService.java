package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.RawProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface RawProductService {

    RawProductResponse createRawProduct(RawProductRequest request);

    RawProductResponse updateRawProduct(Long id, RawProductRequest request);

    void deleteRawProduct(Long id);

    RawProductResponse getRawProductById(Long id);

    List<RawProductResponse> getAllRawProducts();

    RawProductResponse increaseStock(Long id, BigDecimal quantity);

    RawProductResponse decreaseStock(Long id, BigDecimal quantity);

    List<RawProductResponse> getLowStockItems();

    RawProductResponse updateByMaterialCode(String materialCode, RawProductRequest request);
}