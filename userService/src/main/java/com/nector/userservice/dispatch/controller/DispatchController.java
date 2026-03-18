package com.nector.userservice.dispatch.controller;

import com.nector.userservice.dispatch.dto.*;
import com.nector.userservice.dispatch.entity.Gdn;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.dispatch.service.GdnService;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.repository.CartRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dispatch Team", description = "APIs for dispatch team to manage GDN")
public class DispatchController {
    
    private final GdnService gdnService;
    private final GdnRepository gdnRepository;
    private final CartRepository cartRepository;
    private final DistributorRepository distributorRepository;

    @PostMapping("/gdn/generate/{orderId}")
    @Operation(summary = "Generate GDN", description = "Generate Goods Delivery Note for approved order with inventory verification")
    @ApiResponse(responseCode = "201", description = "GDN generated successfully")
    public ResponseEntity<?> generateGdn(@PathVariable Long orderId, @Valid @RequestBody(required = false) SimpleGdnRequest request) {
        log.info("Generating GDN for order: {}", orderId);
        try {

            GdnResponse response = gdnService.generateSimpleGdn(orderId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error generating GDN for order {}: {}", orderId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/inventory/verify/{orderId}")
    @Operation(summary = "Verify Inventory", description = "Verify inventory availability for order before GDN generation")
    @ApiResponse(responseCode = "200", description = "Inventory verification completed")
    public ResponseEntity<?> verifyInventory(@PathVariable Long orderId) {
        log.info("Verifying inventory for order: {}", orderId);
        try {
            InventoryVerificationResponse response = gdnService.verifyInventoryForOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying inventory for order {}: {}", orderId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/gdn/{orderId}")
    @Operation(summary = "Get GDN by Order ID", description = "Retrieve GDN details for a specific order")
    @ApiResponse(responseCode = "200", description = "GDN retrieved successfully")
    public ResponseEntity<?> getGdnByOrderId(@PathVariable Long orderId) {
        log.info("Fetching GDN for order: {}", orderId);
        try {
            GdnResponse response = gdnService.getGdnByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching GDN for order {}: {}", orderId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/gdn/{orderId}/download")
    @Operation(summary = "Download GDN PDF")
    @ApiResponse(
            responseCode = "200",
            description = "PDF downloaded",
            content = @Content(mediaType = "application/pdf")
    )
    public ResponseEntity<byte[]> downloadGdn(
            @PathVariable Long orderId) {

        try {

            byte[] pdfBytes =
                    gdnService
                            .downloadGdnPdf(orderId);

            GdnResponse gdnDetails =
                    gdnService
                            .getGdnByOrderId(orderId);

            String gdnNumber =
                    gdnDetails.getGdnNumber();

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_PDF);

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(gdnNumber.replace("/", "-")+".pdf")
                            .build()
            );

            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch(Exception e){

            log.error("Download failed",e);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @GetMapping("/gdn/{orderId}/url")
    @Operation(summary = "Get GDN PDF URL", description = "Returns direct Cloudinary URL for PDF")
    @ApiResponse(responseCode = "200", description = "PDF URL retrieved successfully")
    @ApiResponse(responseCode = "404", description = "GDN not found for order ID")
    @ApiResponse(responseCode = "200", description = "No GDN exists for this order")
    public ResponseEntity<?> getGdnUrl(@PathVariable Long orderId) {
        try {
            Map<String, Object> response = gdnService.getGdnUrl(orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("GDN not found for order ID")) {
                // Return 200 with proper response when no GDN exists
                Map<String, Object> response = Map.of(
                        "orderId", orderId,
                        "gdnNumber", null,
                        "pdfUrl", null,
                        "hasPdf", false,
                        "message", "No GDN exists for this order ID"
                );
                return ResponseEntity.ok(response);
            } else {
                // Handle other errors (PDF not available, etc.)
                log.error("Failed to retrieve PDF URL for order ID: {} - {}", orderId, e.getMessage());
                Map<String, Object> response = Map.of(
                        "orderId", orderId,
                        "gdnNumber", null,
                        "pdfUrl", null,
                        "hasPdf", false,
                        "message", e.getMessage()
                );
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error for order ID: {} - {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @GetMapping("/gdn/all")
    @Operation(summary = "Get All GDNs", description = "Retrieves all generated Goods Delivery Notes")
    @ApiResponse(responseCode = "200", description = "All GDNs retrieved successfully")
    public ResponseEntity<?> getAllGdns() {
        try {
            List<Map<String, Object>> gdns = gdnService.getAllGdns();
            log.info("Retrieved {} GDNs", gdns.size());
            return ResponseEntity.ok(gdns);
        } catch (Exception e) {
            log.error("Failed to retrieve all GDNs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve GDNs", "message", e.getMessage()));
        }
    }

    @PostMapping("/gdn/{orderId}/regenerate-pdf")
    public ResponseEntity<?> regenerateGdnPdf(@PathVariable Long orderId) {
        try {
            Gdn gdn = gdnRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("GDN not found for order: " + orderId));

            Cart cart = cartRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Cart not found for order: " + orderId));

            gdnService.generateGdnPdf(gdn, cart);

            return ResponseEntity.ok(Map.of("message", "PDF regenerated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/carts/payment-approved")
    @Operation(summary = "Get All Payment Approved Carts", description = "Retrieve all carts with PAYMENT_APPROVED status (excluding GDN_GENERATED)")
    @ApiResponse(responseCode = "200", description = "Payment approved carts retrieved successfully")
    public ResponseEntity<?> getPaymentApprovedCarts() {
        log.info("Fetching all carts with PAYMENT_APPROVED status");
        try {
            List<Cart> carts = cartRepository.findByStatus(Cart.CartStatus.PAYMENT_APPROVED);
            List<CartDto> cartDtos = carts.stream()
                    .map(cart -> {
                        CartDto cartDto = gdnService.convertToCartDto(cart);
                        // Fetch distributor to get salesperson info
                        if (cart.getDistributorId() != null) {
                            distributorRepository.findById(cart.getDistributorId())
                                    .ifPresent(distributor -> {
                                        cartDto.setSalespersonId(distributor.getSalespersonId());
                                        cartDto.setSalespersonName(distributor.getAssignedPerson());
                                    });
                        }
                        return cartDto;
                    })
                    .collect(Collectors.toList());
            log.info("Retrieved {} carts with PAYMENT_APPROVED status", cartDtos.size());
            return ResponseEntity.ok(cartDtos);
        } catch (Exception e) {
            log.error("Error fetching payment approved carts: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/carts/gdn-generated")
    @Operation(summary = "Get All GDN Generated Carts", description = "Retrieve all carts with GDN_GENERATED status")
    @ApiResponse(responseCode = "200", description = "GDN generated carts retrieved successfully")
    public ResponseEntity<?> getGdnGeneratedCarts() {
        log.info("Fetching all carts with GDN_GENERATED status");
        try {
            List<Cart> carts = cartRepository.findByStatus(Cart.CartStatus.GDN_GENERATED);
            List<CartDto> cartDtos = carts.stream()
                    .map(gdnService::convertToCartDto)
                    .collect(Collectors.toList());
            log.info("Retrieved {} carts with GDN_GENERATED status", cartDtos.size());
            return ResponseEntity.ok(cartDtos);
        } catch (Exception e) {
            log.error("Error fetching GDN generated carts: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}