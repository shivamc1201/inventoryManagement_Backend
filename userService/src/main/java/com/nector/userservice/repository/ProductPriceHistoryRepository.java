package com.nector.userservice.repository;

import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {

    List<ProductPriceHistory> findByProductTypeAndProductIdOrderByChangedAtDesc(
            ProductPriceHistory.ProductType productType, Long productId);

    List<ProductPriceHistory> findByProductTypeOrderByChangedAtDesc(
            ProductPriceHistory.ProductType productType);
}
