package com.nector.userservice.bom.controller;

import com.nector.userservice.bom.dto.BomProductionRequestDto;
import com.nector.userservice.bom.dto.BomProductionResponseDto;
import com.nector.userservice.bom.dto.BomRequestDto;
import com.nector.userservice.bom.dto.BomResponseDto;
import com.nector.userservice.bom.dto.BomSummaryDto;
import com.nector.userservice.bom.service.BomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill-of-materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill of Materials Management", description = "Complete API for managing Bill of Materials including creation, updates, and analysis")
public class BomController {

    private final BomService bomService;

    @Operation(summary = "Create new Bill of Materials", description = "Create a new Bill of Materials with components and additional costs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bill of Materials created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error in request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<BomResponseDto> createBillOfMaterials(@Valid @RequestBody BomRequestDto requestDto) {
        log.info("Creating new Bill of Materials for product: {}", requestDto.getFinishedProductName());
        BomResponseDto response = bomService.create(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Bill of Materials", description = "Update an existing Bill of Materials by ID with new components and cost details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill of Materials updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error in request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Bill of Materials not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<BomResponseDto> updateBillOfMaterials(
            @Parameter(description = "Bill of Materials ID") @PathVariable Long id,
            @Valid @RequestBody BomRequestDto requestDto) {
        
        log.info("Updating Bill of Materials with ID: {}", id);
        BomResponseDto response = bomService.update(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Bill of Materials by ID", description = "Retrieve detailed Bill of Materials information including components and costs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill of Materials retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Bill of Materials not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/details/{id}")
    public ResponseEntity<BomResponseDto> getBillOfMaterialsById(@PathVariable Long id) {
        
        log.info("Retrieving Bill of Materials with ID: {}", id);
        BomResponseDto response = bomService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all Bill of Materials", description = "Retrieve paginated list of all Bill of Materials with sorting options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill of Materials list retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    public ResponseEntity<Page<BomResponseDto>> getAllBillOfMaterials(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        
        log.info("Retrieving Bill of Materials list - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(bomService.getAll(pageable));
    }

    @Operation(summary = "Delete Bill of Materials", description = "Delete a Bill of Materials by ID (permanent deletion)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Bill of Materials deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Bill of Materials not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBillOfMaterials(@PathVariable Long id) {
        
        log.info("Deleting Bill of Materials with ID: {}", id);
        bomService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search Bill of Materials", description = "Search Bill of Materials by product name, code, or description with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<BomResponseDto>> searchBillOfMaterials(
            @Parameter(description = "Search keyword for product name, code, or description") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        
        log.info("Searching Bill of Materials with keyword: {} - page: {}, size: {}", keyword, page, size);

        return ResponseEntity.ok(
                bomService.search(keyword, pageable)
        );
    }

    @Operation(summary = "Get Bill of Materials summary", description = "Retrieve comprehensive summary statistics including total count, average costs, and component analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    public ResponseEntity<BomSummaryDto> getBillOfMaterialsSummary() {
        
        log.info("Retrieving Bill of Materials summary statistics");
        
        return ResponseEntity.ok(
                bomService.getSummary()
        );
    }

    @Operation(summary = "Produce finished product using BOM", description = "Produce finished product by consuming raw materials according to BOM recipe. Deducts raw materials from inventory and adds finished product to inventory.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Finished product produced successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or insufficient raw materials"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "BOM or finished product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/produce")
    public ResponseEntity<BomProductionResponseDto> produceFinishedProduct(
            @Valid @RequestBody BomProductionRequestDto requestDto) {
        
        log.info("Producing finished product: {} with quantity: {}", 
                requestDto.getFinishedProductName(), requestDto.getQuantity());
        
        BomProductionResponseDto response = bomService.produceFinishedProduct(requestDto);
        return ResponseEntity.ok(response);
    }

}