package com.nector.userservice.controller;

import com.nector.userservice.dto.DealerSaleRequest;
import com.nector.userservice.model.DealerSale;
import com.nector.userservice.service.DealerSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealer-sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Sales", description = "APIs for managing dealer sales with automatic ledger sync")
public class DealerSaleController {

    private final DealerSaleService dealerSaleService;

    @PostMapping
    @Operation(summary = "Create dealer sale", description = "Create a new dealer sale and automatically generate corresponding ledger entry")
    public ResponseEntity<DealerSale> createDealerSale(
            @RequestBody DealerSaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        DealerSale sale = dealerSaleService.createDealerSale(request, distributorId);
        return ResponseEntity.ok(sale);
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
