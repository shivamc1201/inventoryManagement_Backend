package com.nector.userservice.interceptors.distributor;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.dto.cart.CartResponse;
import com.nector.userservice.interceptors.distributor.model.*;
import com.nector.userservice.interceptors.distributor.service.DistributorService;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.model.DistributorLedger;
import com.nector.userservice.repository.DistributorLedgerRepository;
import com.nector.userservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/distributors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Distributor Management", description = "APIs for managing distributors")
public class DistributorController {

    private final DistributorService distributorService;
    private final DistributorLedgerRepository distributorLedgerRepository;
    private final CartService cartService;

    @PostMapping("/create-distributor")
    @Operation(summary = "Create distributor", description = "Create a new distributor")
    public ResponseEntity<ApiResponse<DistributorResponseDTO>> createDistributor(
            @Valid @RequestBody DistributorRequestDTO request) {
        try {
            DistributorResponseDTO response = distributorService.createDistributor(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Distributor created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating distributor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create distributor", null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get distributor by ID", description = "Retrieve a distributor by its ID")
    public ResponseEntity<ApiResponse<DistributorResponseDTO>> getDistributorById(@PathVariable Long id) {
        try {
            DistributorResponseDTO response = distributorService.getDistributorById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributor retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving distributor with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all distributors", description = "Retrieve all distributors")
    public ResponseEntity<ApiResponse<List<DistributorResponseDTO>>> getAllDistributors() {
        try {
            List<DistributorResponseDTO> response = distributorService.getAllDistributors();
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributors retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving distributors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve distributors", null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update distributor", description = "Update an existing distributor")
    public ResponseEntity<ApiResponse<DistributorResponseDTO>> updateDistributor(
            @PathVariable Long id,
            @Valid @RequestBody DistributorRequestDTO request) {
        try {
            DistributorResponseDTO response = distributorService.updateDistributor(id, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributor updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating distributor with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update distributor", null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete distributor", description = "Delete a distributor by ID")
    public ResponseEntity<ApiResponse<Void>> deleteDistributor(@PathVariable Long id) {
        try {
            distributorService.deleteDistributor(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributor deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting distributor with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete distributor", null));
        }
    }

    @PostMapping("/{distributorId}/confirm-order")
    @Operation(summary = "Confirm order received", description = "Distributor confirms order receipt and provides feedback")
    public ResponseEntity<ApiResponse<OrderConfirmationResponse>> confirmOrderReceived(
            @PathVariable Long distributorId,
            @Valid @RequestBody OrderConfirmationRequest request) {
        try {
            OrderConfirmationResponse response = distributorService.confirmOrderReceived(distributorId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Order confirmation recorded successfully", response));
        } catch (Exception e) {
            log.error("Error confirming order for distributor {}: {}", distributorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/order-confirmation/{orderId}")
    @Operation(summary = "Get order confirmation", description = "Get order confirmation details by order ID")
    public ResponseEntity<ApiResponse<OrderConfirmationResponse>> getOrderConfirmation(@PathVariable Long orderId) {
        try {
            OrderConfirmationResponse response = distributorService.getOrderConfirmation(orderId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Order confirmation retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving order confirmation for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{distributorId}/confirmations")
    @Operation(summary = "Get distributor confirmations", description = "Get all order confirmations for a distributor")
    public ResponseEntity<ApiResponse<List<OrderConfirmationResponse>>> getDistributorConfirmations(@PathVariable Long distributorId) {
        try {
            List<OrderConfirmationResponse> response = distributorService.getDistributorConfirmations(distributorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributor confirmations retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving confirmations for distributor {}: {}", distributorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve confirmations", null));
        }
    }

    @GetMapping("/{distributorId}/balance")
    @Operation(summary = "Get distributor current balance", description = "Get current balance from distributor_ledger table")
    public ResponseEntity<ApiResponse<BigDecimal>> getDistributorCurrentBalance(@PathVariable Long distributorId) {
        try {
            BigDecimal balance = distributorLedgerRepository.getDistributorBalance(distributorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Distributor balance retrieved successfully", balance));
        } catch (Exception e) {
            log.error("Error retrieving balance for distributor {}: {}", distributorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve distributor balance", null));
        }
    }

    @GetMapping("/api/distributorsActiveCart")
    public ResponseEntity<?> getDistributorActiveCart(@RequestParam Long distributorId) {
        log.info("Fetching cart for distributor: {}", distributorId);
        try {
            CartResponse response = cartService.getActiveCartByDistributorId(distributorId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching cart for distributor {}: {}", distributorId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

    }
}
