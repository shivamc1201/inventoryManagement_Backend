package com.nector.userservice.transaction.controller;

import com.nector.userservice.transaction.dto.CashbookSummary;
import com.nector.userservice.transaction.service.CashbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction Cashbook", description = "Cashbook summary for the Transaction Cashbook page")
public class CashbookController {

    private final CashbookService cashbookService;

    @GetMapping("/cashbook")
    @Operation(summary = "Get cashbook summary for a date range")
    public ResponseEntity<CashbookSummary> getCashbook(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(cashbookService.getCashbook(from, to));
    }
}
