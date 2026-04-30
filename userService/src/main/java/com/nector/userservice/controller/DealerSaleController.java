package com.nector.userservice.controller;

import com.nector.userservice.dto.DealerOrderRequest;
import com.nector.userservice.dto.DealerSaleRequest;
import com.nector.userservice.model.DealerOrder;
import com.nector.userservice.model.DealerSale;
import com.nector.userservice.service.DealerSaleService;
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
@RequestMapping("/api/dealer-sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Sales", description = "APIs for managing dealer sales with automatic ledger sync")
public class DealerSaleController {

    private final DealerSaleService dealerSaleService;

    @PostMapping
    @Operation(summary = "Create dealer sale", description = "Create a new dealer sale and automatically generate corresponding ledger entry")
    public ResponseEntity<DealerSale> createDealerSale(
            @RequestBody DealerSaleRequest request,
            @Parameter(description = "Distributor ID for the dealer") 
            @RequestParam Long distributorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        DealerSale sale = dealerSaleService.createDealerSale(request, distributorId);
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Get sales by dealer ID", description = "Retrieve all sales for a specific dealer")
    public ResponseEntity<List<DealerSale>> getSalesByDealerId(
            @Parameter(description = "Dealer ID") 
            @PathVariable Long dealerId) {
        
        List<DealerSale> sales = dealerSaleService.getSalesByDealerId(dealerId);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/distributor/{distributorId}")
    @Operation(summary = "Get sales by distributor ID", description = "Retrieve all sales for a specific distributor")
    public ResponseEntity<List<DealerSale>> getSalesByDistributorId(
            @Parameter(description = "Distributor ID")
            @PathVariable Long distributorId) {

        List<DealerSale> sales = dealerSaleService.getSalesByDistributorId(distributorId);
        return ResponseEntity.ok(sales);
    }

    @PostMapping("/orders")
    @Operation(summary = "Create dealer order", description = "Create a new dealer order with quantity")
    public ResponseEntity<DealerOrder> createDealerOrder(
            @RequestBody DealerOrderRequest request,
            @Parameter(description = "Distributor ID for the dealer")
            @RequestParam Long distributorId,
            @AuthenticationPrincipal UserDetails userDetails) {

        DealerOrder order = dealerSaleService.createDealerOrder(request, distributorId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/dealer/{dealerId}")
    @Operation(summary = "Get orders by dealer ID", description = "Retrieve all orders for a specific dealer")
    public ResponseEntity<List<DealerOrder>> getOrdersByDealerId(
            @Parameter(description = "Dealer ID")
            @PathVariable Long dealerId) {

        List<DealerOrder> orders = dealerSaleService.getOrdersByDealerId(dealerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/distributor/{distributorId}")
    @Operation(summary = "Get orders by distributor ID", description = "Retrieve all orders for a specific distributor")
    public ResponseEntity<List<DealerOrder>> getOrdersByDistributorId(
            @Parameter(description = "Distributor ID")
            @PathVariable Long distributorId) {

        List<DealerOrder> orders = dealerSaleService.getOrdersByDistributorId(distributorId);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{saleId}")
    @Operation(summary = "Delete dealer sale", description = "Delete a dealer sale by ID")
    public ResponseEntity<Void> deleteDealerSale(
            @Parameter(description = "Sale ID to delete")
            @PathVariable Long saleId,
            @Parameter(description = "Distributor ID for tenant isolation")
            @RequestParam Long distributorId,
            @AuthenticationPrincipal UserDetails userDetails) {

        dealerSaleService.deleteDealerSale(saleId, distributorId);
        return ResponseEntity.noContent().build();
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
