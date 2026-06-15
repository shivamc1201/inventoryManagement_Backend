package com.nector.userservice.transaction.controller;

import com.nector.userservice.transaction.dto.LedgerRequest;
import com.nector.userservice.transaction.dto.LedgerResponse;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.service.TransactionLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction Ledger", description = "Create and list ledgers for the Transaction Master")
public class TransactionLedgerController {

    private final TransactionLedgerService ledgerService;

    @PostMapping("/ledger")
    @Operation(summary = "Create a transaction ledger")
    public ResponseEntity<LedgerResponse> createLedger(@Valid @RequestBody LedgerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.createLedger(request));
    }

    @GetMapping("/ledgers")
    @Operation(summary = "List ledgers, optionally filtered by type")
    public ResponseEntity<List<LedgerResponse>> getLedgers(
            @RequestParam(value = "type", required = false) LedgerType type) {
        List<LedgerResponse> ledgers = type == null
                ? ledgerService.getAllLedgers()
                : ledgerService.getLedgersByType(type);
        return ResponseEntity.ok(ledgers);
    }
}
