package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.FinishedProductRequest;
import com.nector.userservice.interceptors.products.model.FinishedProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FinishedProductService {
    
    FinishedProductResponse createFinishedProduct(FinishedProductRequest request, MultipartFile image);
    
    FinishedProductResponse updateFinishedProduct(Long id, FinishedProductRequest request,MultipartFile image);
    
    void deleteFinishedProduct(Long id);
    
    FinishedProductResponse getFinishedProductById(Long id);
    
    List<FinishedProductResponse> getAllActiveFinishedProducts();
    
    FinishedProductResponse increaseStock(Long id, Integer quantity);
    
    FinishedProductResponse decreaseStock(Long id, Integer quantity);
    
    List<FinishedProductResponse> getLowStockItems();

    FinishedProductResponse updateProductStatus(Long id, Boolean status);

    List<FinishedProductResponse> getAllFinishedProductsWithoutStatusCheck();
}