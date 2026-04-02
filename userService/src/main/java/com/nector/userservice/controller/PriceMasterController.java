package com.nector.userservice.controller;

import com.nector.userservice.model.PriceMasterProduct;
import com.nector.userservice.service.PriceMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-master")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price Master", description = "APIs for managing product pricing")
public class PriceMasterController {

    private final PriceMasterService priceMasterService;

    @GetMapping
    @Operation(summary = "Get price master for dealer", description = "Fetch product prices for a specific dealer")
    public ResponseEntity<List<PriceMasterProduct>> getPriceMasterForDealer(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        List<PriceMasterProduct> priceMaster = priceMasterService.getPriceMasterForDealer(dealerId, distributorId);
        return ResponseEntity.ok(priceMaster);
    }

    @GetMapping("/search")
    @Operation(summary = "Search price master", description = "Search products in price master")
    public ResponseEntity<List<PriceMasterProduct>> searchPriceMaster(
            @Parameter(description = "Search term") 
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        List<PriceMasterProduct> results = priceMasterService.searchPriceMaster(distributorId, search);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @Operation(summary = "Add product to price master", description = "Add a single product to price master")
    public ResponseEntity<PriceMasterProduct> addProductToPriceMaster(
            @RequestBody PriceMasterProduct product,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        PriceMasterProduct saved = priceMasterService.addProductToPriceMaster(product, distributorId);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk add to price master", description = "Batch save product pricing (Append-only versioning)")
    public ResponseEntity<List<PriceMasterProduct>> bulkAddToPriceMaster(
            @RequestBody List<PriceMasterProduct> products,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        List<PriceMasterProduct> saved = priceMasterService.bulkAddToPriceMaster(products, distributorId);
        return ResponseEntity.ok(saved);
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
