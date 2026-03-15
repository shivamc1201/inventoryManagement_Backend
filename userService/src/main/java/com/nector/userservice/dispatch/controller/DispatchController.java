package com.nector.userservice.dispatch.controller;

import com.nector.userservice.dispatch.dto.GdnGenerationRequest;
import com.nector.userservice.dispatch.dto.SimpleGdnRequest;
import com.nector.userservice.dispatch.dto.GdnRequest;
import com.nector.userservice.dispatch.dto.GdnResponse;
import com.nector.userservice.dispatch.dto.InventoryVerificationResponse;
import com.nector.userservice.dispatch.service.GdnService;
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

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dispatch Team", description = "APIs for dispatch team to manage GDN")
public class DispatchController {
    
    private final GdnService gdnService;

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
    @ApiResponse(responseCode = "404", description = "GDN or PDF URL not found")
    public ResponseEntity<?> getGdnUrl(@PathVariable Long orderId) {
        try {
            Map<String, Object> response = gdnService.getGdnUrl(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve PDF URL for order ID: {} - {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "GDN not found", "message", e.getMessage()));
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
}