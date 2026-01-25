package com.nector.userservice.interceptors.distributor;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.interceptors.distributor.model.DistributorRequestDTO;
import com.nector.userservice.interceptors.distributor.model.DistributorResponseDTO;
import com.nector.userservice.interceptors.distributor.service.DistributorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distributors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Distributor Management", description = "APIs for managing distributors")
public class DistributorController {

    private final DistributorService distributorService;

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
}
