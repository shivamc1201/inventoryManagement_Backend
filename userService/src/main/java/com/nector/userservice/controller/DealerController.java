package com.nector.userservice.controller;

import com.nector.userservice.dto.DealerCreateRequest;
import com.nector.userservice.dto.DealerResponse;
import com.nector.userservice.dto.DealerUpdateRequest;
import com.nector.userservice.service.DealerService;
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
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Management", description = "APIs for managing dealers")
public class DealerController {

    private final DealerService dealerService;

    @PostMapping
    @Operation(summary = "Create a new dealer", description = "Register a new dealer for a specific distributor")
    public ResponseEntity<DealerResponse> createDealer(
            @RequestBody DealerCreateRequest request,
            @Parameter(description = "Distributor ID to assign the dealer to") 
            @RequestParam Long distributorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        DealerResponse response = dealerService.createDealer(request, distributorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all active dealers", description = "Retrieve all active dealers for the authenticated distributor")
    public ResponseEntity<List<DealerResponse>> getDealers(
            @Parameter(description = "Search term to filter dealers by name or phone") 
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        List<DealerResponse> dealers = dealerService.getActiveDealers(distributorId, search);
        return ResponseEntity.ok(dealers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dealer by ID", description = "Retrieve a specific dealer by ID")
    public ResponseEntity<DealerResponse> getDealerById(
            @Parameter(description = "Dealer ID") 
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        DealerResponse dealer = dealerService.getDealerById(id, distributorId);
        return ResponseEntity.ok(dealer);
    }

    @GetMapping("/distributor/{distributorId}")
    @Operation(summary = "Get dealers by distributor ID", description = "Retrieve all dealers for a specific distributor")
    public ResponseEntity<List<DealerResponse>> getDealersByDistributorId(
            @Parameter(description = "Distributor ID") 
            @PathVariable Long distributorId) {
        
        List<DealerResponse> dealers = dealerService.getDealersByDistributorId(distributorId);
        return ResponseEntity.ok(dealers);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update dealer", description = "Update dealer details")
    public ResponseEntity<DealerResponse> updateDealer(
            @Parameter(description = "Dealer ID") 
            @PathVariable Long id,
            @RequestBody DealerUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        DealerResponse dealer = dealerService.updateDealer(id, request, distributorId);
        return ResponseEntity.ok(dealer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete dealer", description = "Deactivate a dealer (soft delete)")
    public ResponseEntity<Void> deleteDealer(
            @Parameter(description = "Dealer ID") 
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        dealerService.softDeleteDealer(id, distributorId);
        return ResponseEntity.ok().build();
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        // For now, returning a placeholder - you'll need to implement this based on your auth system
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
