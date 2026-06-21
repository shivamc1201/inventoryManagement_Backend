package com.nector.userservice.interceptors.products;

import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import com.nector.userservice.interceptors.products.service.ProductPriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/price-history")
@RequiredArgsConstructor
public class ProductPriceHistoryController {

    private final ProductPriceHistoryService productPriceHistoryService;

    /** All price history for one specific product. */
    @GetMapping("/{productType}/{productId}")
    public ResponseEntity<List<ProductPriceHistory>> getHistory(
            @PathVariable ProductPriceHistory.ProductType productType,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productPriceHistoryService.getHistory(productType, productId));
    }

    /** All price history for a product type (e.g. all MACHINE_PART price changes). */
    @GetMapping("/{productType}")
    public ResponseEntity<List<ProductPriceHistory>> getAllByType(
            @PathVariable ProductPriceHistory.ProductType productType) {
        return ResponseEntity.ok(productPriceHistoryService.getAllByType(productType));
    }
}
