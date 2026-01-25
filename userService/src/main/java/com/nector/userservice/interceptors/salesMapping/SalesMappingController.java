package com.nector.userservice.interceptors.salesMapping;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.interceptors.salesMapping.model.CreateMappingRequestDTO;
import com.nector.userservice.interceptors.salesMapping.model.MappingResponseDTO;
import com.nector.userservice.interceptors.salesMapping.service.SalesMappingService;
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
@RequestMapping("/api/sales-mapping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Mapping", description = "APIs for managing salesperson-distributor mappings")
public class SalesMappingController {

    private final SalesMappingService salesMappingService;

    @PostMapping
    @Operation(summary = "Create mapping", description = "Create salesperson-distributor mapping and ledger account")
    public ResponseEntity<ApiResponse<MappingResponseDTO>> createMapping(
            @Valid @RequestBody CreateMappingRequestDTO request) {
        try {
            String createdBy = "admin"; // Get from security context
            MappingResponseDTO response = salesMappingService.createMapping(request, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Mapping created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating mapping", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create mapping", null));
        }
    }

    @GetMapping("/salesperson/{salespersonId}")
    @Operation(summary = "Get mappings by salesperson", description = "Get all mappings for a salesperson")
    public ResponseEntity<ApiResponse<List<MappingResponseDTO>>> getMappingsBySalesperson(@PathVariable Long salespersonId) {
        try {
            List<MappingResponseDTO> response = salesMappingService.getMappingsBySalesperson(salespersonId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Mappings retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving mappings for salesperson: {}", salespersonId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve mappings", null));
        }
    }

    @GetMapping("/distributor/{distributorId}")
    @Operation(summary = "Get mappings by distributor", description = "Get all mappings for a distributor")
    public ResponseEntity<ApiResponse<List<MappingResponseDTO>>> getMappingsByDistributor(@PathVariable Long distributorId) {
        try {
            List<MappingResponseDTO> response = salesMappingService.getMappingsByDistributor(distributorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Mappings retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving mappings for distributor: {}", distributorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve mappings", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all mappings", description = "Get all salesperson-distributor mappings")
    public ResponseEntity<ApiResponse<List<MappingResponseDTO>>> getAllMappings() {
        try {
            List<MappingResponseDTO> response = salesMappingService.getAllMappings();
            return ResponseEntity.ok(new ApiResponse<>(true, "All mappings retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error retrieving all mappings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve mappings", null));
        }
    }

    @PutMapping("/{mappingId}/deactivate")
    @Operation(summary = "Deactivate mapping", description = "Deactivate a salesperson-distributor mapping")
    public ResponseEntity<ApiResponse<Void>> deactivateMapping(@PathVariable Long mappingId) {
        try {
            salesMappingService.deactivateMapping(mappingId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Mapping deactivated successfully", null));
        } catch (Exception e) {
            log.error("Error deactivating mapping: {}", mappingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to deactivate mapping", null));
        }
    }
}