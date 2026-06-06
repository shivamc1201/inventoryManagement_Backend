package com.nector.userservice.controller;

import com.nector.userservice.dto.DealerOrderRequest;
import com.nector.userservice.dto.DealerSaleRequest;
import com.nector.userservice.model.DealerOrder;
import com.nector.userservice.model.DealerSale;
import com.nector.userservice.service.DealerInvoiceService;
import com.nector.userservice.service.DealerSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/dealer-sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Sales", description = "APIs for managing dealer sales with automatic ledger sync")
public class DealerSaleController {

    private final DealerSaleService dealerSaleService;
    private final DealerInvoiceService dealerInvoiceService;

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

    @GetMapping("/orders/{orderId}/invoice")
    @Operation(summary = "Get dealer invoice by order ID", description = "Retrieve invoice details and PDF URL for a dealer order")
    public ResponseEntity<Map<String, Object>> getDealerInvoice(
            @Parameter(description = "Dealer Order ID")
            @PathVariable Long orderId) {

        Map<String, Object> invoice = dealerInvoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/orders/{orderId}/invoice/download")
    @Operation(summary = "Download dealer invoice PDF", description = "Download the invoice PDF for a dealer order")
    public ResponseEntity<byte[]> downloadDealerInvoice(
            @Parameter(description = "Dealer Order ID")
            @PathVariable Long orderId) {

        byte[] pdfBytes = dealerInvoiceService.downloadInvoicePdf(orderId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "dealer-invoice-" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/orders/{orderId}/regenerate-pdf")
    @Operation(summary = "Regenerate dealer invoice PDF", description = "Regenerate and re-upload the dealer invoice PDF to cloud storage")
    public ResponseEntity<Map<String, Object>> regenerateDealerInvoicePdf(
            @Parameter(description = "Dealer Order ID")
            @PathVariable Long orderId) {
        try {
            dealerInvoiceService.regeneratePdf(orderId);
            return ResponseEntity.ok(Map.of("message", "Dealer invoice PDF regenerated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
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
