package com.nector.userservice.ledger.controller;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.ledger.dto.*;
import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.enums.TransactionType;
import com.nector.userservice.ledger.service.LedgerAccountService;
import com.nector.userservice.ledger.service.LedgerExportService;
import com.nector.userservice.ledger.service.LedgerTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

/**
 * REST Controller for Ledger Management
 * Provides APIs for ledger accounts and transactions
 */
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ledger Management", description = "APIs for managing financial ledger accounts and transactions")
public class LedgerController {

    private final LedgerAccountService ledgerAccountService;
    private final LedgerTransactionService ledgerTransactionService;
    private final LedgerExportService ledgerExportService;

    /**
     * Create a new ledger account
     */
    @PostMapping("/register-account")
    @Operation(summary = "Create ledger account", description = "Create a new ledger account for a distributor")
    public ResponseEntity<ApiResponse<LedgerAccount>> createLedgerAccount(
            @Valid @RequestBody CreateLedgerAccountRequest request) {

        try {
            String createdBy = "system";
            LedgerAccount account = ledgerAccountService.createLedgerAccount(request, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Ledger account created successfully", account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating ledger account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create ledger account", null));
        }
    }

    /**
     * Get ledger account by company and distributor ID
     */
    @GetMapping("/find-account")
    @Operation(summary = "Get ledger account", description = "Get ledger account by company and distributor ID")
    public ResponseEntity<ApiResponse<LedgerAccount>> getLedgerAccount(
            @RequestParam Long companyId,
            @RequestParam Long distributorId) {

        try {
            LedgerAccount account = ledgerAccountService.getLedgerAccount(companyId, distributorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Account retrieved successfully", account));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving ledger account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve ledger account", null));
        }
    }

    /**
     * Get ledger summary
     */
    @GetMapping("/accounts/{accountId}/financial-summary")
    @Operation(summary = "Get ledger summary", description = "Get financial summary for a ledger account")
    public ResponseEntity<ApiResponse<LedgerSummaryResponse>> getLedgerSummary(
            @PathVariable Long accountId) {

        try {
            LedgerSummaryResponse summary = ledgerAccountService.getLedgerSummary(accountId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Summary retrieved successfully", summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving ledger summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve ledger summary", null));
        }
    }

    /**
     * Create a new transaction
     */
    @PostMapping("/accounts/{accountId}/record-transaction")
    @Operation(summary = "Create transaction", description = "Create a new ledger transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateTransactionRequest request) {

        try {
            String createdBy = "system";
            TransactionResponse transaction = ledgerTransactionService.createTransaction(accountId, request, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Transaction created successfully", transaction));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create transaction", null));
        }
    }

    /**
     * Get transactions with pagination and filters
     */
    @GetMapping("/accounts/{accountId}/transaction-history")
    @Operation(summary = "Get transactions", description = "Get paginated list of transactions with optional filters")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) String search) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TransactionResponse> transactions = ledgerTransactionService.getTransactionsWithFilters(
                    accountId, startDate, endDate, transactionType, search, pageable);

            return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            log.error("Error retrieving transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve transactions", null));
        }
    }

    /**
     * Reverse a transaction
     */
    @PostMapping("/transactions/{transactionId}/cancel")
    @Operation(summary = "Reverse transaction", description = "Create a reversal transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @PathVariable Long transactionId,
            @RequestParam String reason) {

        try {
            String createdBy = "system";
            TransactionResponse reversal = ledgerTransactionService.reverseTransaction(transactionId, reason, createdBy);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Transaction reversed successfully", reversal));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error reversing transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to reverse transaction", null));
        }
    }

    /**
     * Export transactions to CSV
     */
    @GetMapping("/accounts/{accountId}/download-report")
    @Operation(summary = "Export transactions", description = "Export transactions to CSV format")
    public ResponseEntity<byte[]> exportTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType transactionType) {

        try {
            byte[] exportData;
            String contentType;

            if ("csv".equalsIgnoreCase(format)) {
                exportData = ledgerExportService.exportTransactionsToCSV(accountId, startDate, endDate, transactionType);
                contentType = "text/csv";
            } else {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=transactions.csv")
                    .body(exportData);
        } catch (Exception e) {
            log.error("Error exporting transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
