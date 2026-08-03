package com.nector.userservice.interceptors.products;

import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import com.nector.userservice.interceptors.products.service.ProductPriceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/price-history")
@RequiredArgsConstructor
@Slf4j
public class ProductPriceHistoryController {

    private final ProductPriceHistoryService productPriceHistoryService;

    @GetMapping("/{productType}/{productId}")
    public ResponseEntity<List<ProductPriceHistory>> getHistory(
            @PathVariable ProductPriceHistory.ProductType productType,
            @PathVariable Long productId) {
        log.info("Entering getHistory() - productType: {}, productId: {}", productType, productId);
        List<ProductPriceHistory> history = productPriceHistoryService.getHistory(productType, productId);
        log.info("Exiting getHistory() - returned {} records for productType: {}, productId: {}", history.size(), productType, productId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{productType}")
    public ResponseEntity<List<ProductPriceHistory>> getAllByType(
            @PathVariable ProductPriceHistory.ProductType productType) {
        log.info("Entering getAllByType() - productType: {}", productType);
        List<ProductPriceHistory> history = productPriceHistoryService.getAllByType(productType);
        log.info("Exiting getAllByType() - returned {} records for productType: {}", history.size(), productType);
        return ResponseEntity.ok(history);
    }
}
