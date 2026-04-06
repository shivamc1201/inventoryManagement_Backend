package com.nector.userservice.controller;

import com.nector.userservice.dto.LedgerTransactionRequest;
import com.nector.userservice.dto.LedgerTransactionResponse;
import com.nector.userservice.dto.LedgerSummaryResponse;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.DealerRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dealer-ledger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dealer Ledger", description = "APIs for managing dealer financial ledger")
public class DealerLedgerController {

    private final DealerLedgerService dealerLedgerService;
    private final DealerRepository dealerRepository;
    private final DealerLedgerTransactionRepository ledgerTransactionRepository;

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

    @GetMapping("/by-distributor")
    @Operation(summary = "Get dealer ledger by distributor", description = "Retrieve paginated ledger transactions for a dealer with explicit distributorId")
    public ResponseEntity<Page<LedgerTransactionResponse>> getDealerLedgerByDistributor(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @Parameter(description = "Distributor ID") 
            @RequestParam Long distributorId,
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerTransactionResponse> ledger = dealerLedgerService.getDealerLedger(
                dealerId, distributorId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ledger);
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

    @GetMapping("/summary/by-distributor")
    @Operation(summary = "Get ledger summary by distributor", description = "Get KPI summary for a dealer's ledger with explicit distributorId")
    public ResponseEntity<LedgerSummaryResponse> getLedgerSummaryByDistributor(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @Parameter(description = "Distributor ID") 
            @RequestParam Long distributorId) {
        
        LedgerSummaryResponse summary = dealerLedgerService.getLedgerSummary(dealerId, distributorId);
        return ResponseEntity.ok(summary);
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

    @GetMapping("/debug")
    @Operation(summary = "Debug dealer info", description = "Debug endpoint to check dealer and ledger info")
    public ResponseEntity<Map<String, Object>> debugDealer(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // Check if dealer exists and belongs to distributor
            Dealer dealer = dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                    .orElse(null);
            
            if (dealer != null) {
                debugInfo.put("dealerExists", true);
                debugInfo.put("dealerId", dealer.getId());
                debugInfo.put("dealerName", dealer.getFullName());
                debugInfo.put("dealerDistributorId", dealer.getDistributorId());
                debugInfo.put("requestedDistributorId", distributorId);
                debugInfo.put("isActive", dealer.getIsActive());
                
                // Check ledger transactions
                List<DealerLedgerTransaction> transactions = ledgerTransactionRepository
                        .findByDealerIdAndDistributorIdOrderByDateDescCreatedAtDesc(dealerId, distributorId);
                
                debugInfo.put("transactionCount", transactions.size());
                debugInfo.put("transactions", transactions.stream()
                        .map(t -> Map.of(
                            "id", t.getId(),
                            "date", t.getDate().toString(),
                            "description", t.getDescription(),
                            "balance", t.getBalance()
                        ))
                        .collect(Collectors.toList()));
                        
            } else {
                debugInfo.put("dealerExists", false);
                debugInfo.put("dealerId", dealerId);
                debugInfo.put("requestedDistributorId", distributorId);
                
                // Check if dealer exists with different distributor
                Dealer anyDealer = dealerRepository.findById(dealerId).orElse(null);
                if (anyDealer != null) {
                    debugInfo.put("existsWithDifferentDistributor", true);
                    debugInfo.put("actualDistributorId", anyDealer.getDistributorId());
                } else {
                    debugInfo.put("existsWithDifferentDistributor", false);
                }
            }
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/initialize-balance")
    @Operation(summary = "Initialize opening balance", description = "Initialize opening balance for a dealer if not exists")
    public ResponseEntity<Map<String, Object>> initializeOpeningBalance(
            @Parameter(description = "Dealer ID") 
            @RequestParam Long dealerId,
            @Parameter(description = "Opening balance amount") 
            @RequestParam(defaultValue = "0") java.math.BigDecimal openingBalance,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long distributorId = getDistributorIdFromUser(userDetails);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Verify dealer exists and belongs to distributor
            Dealer dealer = dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                    .orElseThrow(() -> new RuntimeException("Dealer not found or access denied"));
            
            // Initialize opening balance
            dealerLedgerService.initializeOpeningBalance(dealerId, distributorId, openingBalance);
            
            result.put("success", true);
            result.put("message", "Opening balance initialized successfully");
            result.put("dealerId", dealerId);
            result.put("distributorId", distributorId);
            result.put("openingBalance", openingBalance);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    private Long getDistributorIdFromUser(UserDetails userDetails) {
        // This should extract distributorId from the authenticated user
        // Implementation depends on your user authentication structure
        return 1L; // Placeholder - replace with actual extraction logic
    }
}
