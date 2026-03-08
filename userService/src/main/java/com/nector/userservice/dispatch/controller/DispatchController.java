package com.nector.userservice.dispatch.controller;

import com.nector.userservice.dispatch.dto.GdnGenerationRequest;
import com.nector.userservice.dispatch.dto.SimpleGdnRequest;
import com.nector.userservice.dispatch.dto.GdnRequest;
import com.nector.userservice.dispatch.dto.GdnResponse;
import com.nector.userservice.dispatch.dto.InventoryVerificationResponse;
import com.nector.userservice.dispatch.service.GdnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
}