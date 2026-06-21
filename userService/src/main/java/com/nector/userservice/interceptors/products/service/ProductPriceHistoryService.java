package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import com.nector.userservice.repository.ProductPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPriceHistoryService {

    private final ProductPriceHistoryRepository repository;

    @Transactional
    public void record(ProductPriceHistory.ProductType type, Long productId,
                       String productName, String productCode,
                       BigDecimal oldPrice, BigDecimal newPrice) {
        BigDecimal changePercent = BigDecimal.ZERO;
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) != 0) {
            changePercent = newPrice.subtract(oldPrice)
                    .divide(oldPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        ProductPriceHistory history = ProductPriceHistory.builder()
                .productType(type)
                .productId(productId)
                .productName(productName)
                .productCode(productCode)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceChangePercent(changePercent)
                .build();
        repository.save(history);
        log.info("Recorded price change for {} '{}' (ID {}): {} → {} ({}%)",
                type, productName, productId, oldPrice, newPrice, changePercent);
    }

    @Transactional(readOnly = true)
    public List<ProductPriceHistory> getHistory(ProductPriceHistory.ProductType type, Long productId) {
        return repository.findByProductTypeAndProductIdOrderByChangedAtDesc(type, productId);
    }

    @Transactional(readOnly = true)
    public List<ProductPriceHistory> getAllByType(ProductPriceHistory.ProductType type) {
        return repository.findByProductTypeOrderByChangedAtDesc(type);
    }
}
