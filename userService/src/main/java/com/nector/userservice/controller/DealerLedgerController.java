package com.nector.userservice.controller;

import com.nector.userservice.dto.LedgerTransactionRequest;
import com.nector.userservice.dto.LedgerTransactionResponse;
import com.nector.userservice.dto.LedgerSummaryResponse;
import com.nector.userservice.service.DealerLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dealer-ledger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Ledger", description = "APIs for managing dealer financial ledger")
public class DealerLedgerController {

    private final DealerLedgerService dealerLedgerService;

    @PostMapping
    @Operation(summary = "Create manual ledger transaction", description = "Create a manual ledger entry for payments or journal adjustments")
    public ResponseEntity<LedgerTransactionResponse> createManualTransaction(
            @RequestBody LedgerTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Use distributorId from request
        Long distributorId = request.getDistributorId();
        
        LedgerTransactionResponse response = dealerLedgerService.createManualTransaction(request, distributorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get dealer ledger", description = "Retrieve paginated ledger transactions for a dealer")
    public ResponseEntity<Page<LedgerTransactionResponse>> getDealerLedger(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerTransactionResponse> ledger = dealerLedgerService.getDealerLedger(
                dealerId, distributorId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get ledger summary", description = "Get KPI summary for a dealer's ledger")
    public ResponseEntity<LedgerSummaryResponse> getLedgerSummary(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        LedgerSummaryResponse summary = dealerLedgerService.getLedgerSummary(dealerId, distributorId);
        return ResponseEntity.ok(summary);
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
