package com.nector.userservice.controller;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "APIs for managing invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get invoice by order ID", description = "Retrieve invoice details for a specific order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInvoiceByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            Map<String, Object> invoice = invoiceService.getInvoiceDetailsByOrder(orderId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Invoice retrieved successfully", invoice));
        } catch (Exception e) {
            log.error("Error retrieving invoice for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/confirmation/{orderConfirmationId}")
    @Operation(summary = "Get invoice by order confirmation ID", description = "Retrieve invoice details for a specific order confirmation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInvoiceByOrderConfirmation(
            @Parameter(description = "Order Confirmation ID") @PathVariable Long orderConfirmationId) {
        try {
            Map<String, Object> invoice = invoiceService.getInvoiceDetailsByOrderConfirmation(orderConfirmationId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Invoice retrieved successfully", invoice));
        } catch (Exception e) {
            log.error("Error retrieving invoice for order confirmation {}: {}", orderConfirmationId, e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/distributor/{distributorId}")
    @Operation(summary = "Get invoices by distributor", description = "Retrieve all invoices for a specific distributor")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInvoicesByDistributor(
            @Parameter(description = "Distributor ID") @PathVariable Long distributorId) {
        try {
            List<Map<String, Object>> invoices = invoiceService.getInvoicesByDistributor(distributorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Invoices retrieved successfully", invoices));
        } catch (Exception e) {
            log.error("Error retrieving invoices for distributor {}: {}", distributorId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Failed to retrieve invoices", null));
        }
    }

    @PostMapping("/order/{orderId}/regenerate-pdf")
    @Operation(summary = "Regenerate invoice PDF", description = "Regenerate and re-upload the invoice PDF to cloud storage from live order data")
    public ResponseEntity<Map<String, Object>> regenerateInvoicePdf(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            invoiceService.regenerateInvoicePdf(orderId);
            return ResponseEntity.ok(Map.of("message", "Invoice PDF regenerated successfully"));
        } catch (Exception e) {
            log.error("Error regenerating invoice PDF for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}/download")
    @Operation(summary = "Download invoice PDF", description = "Download invoice PDF for a specific order")
    public ResponseEntity<ByteArrayResource> downloadInvoicePdf(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            log.info("Downloading invoice PDF for order ID: {}", orderId);
            
            byte[] pdfBytes = invoiceService.downloadInvoicePdf(orderId);
            
            // Get invoice details to construct filename
            Map<String, Object> invoiceDetails = invoiceService.getInvoiceDetailsByOrder(orderId);
            String invoiceNumber = (String) invoiceDetails.get("invoiceNumber");
            
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + invoiceNumber + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading invoice PDF for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }
}
