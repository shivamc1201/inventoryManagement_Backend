package com.nector.userservice.transaction.controller;

import com.nector.userservice.transaction.dto.FundRequest;
import com.nector.userservice.transaction.dto.FundResponse;
import com.nector.userservice.transaction.service.TransactionFundService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction Fund", description = "Add and list funds (Add Fund tab)")
public class TransactionFundController {

    private final TransactionFundService fundService;

    @PostMapping("/fund")
    @Operation(summary = "Add a fund")
    public ResponseEntity<FundResponse> createFund(@Valid @RequestBody FundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fundService.createFund(request));
    }

    @GetMapping("/funds")
    @Operation(summary = "List all funds")
    public ResponseEntity<List<FundResponse>> getFunds() {
        return ResponseEntity.ok(fundService.getAllFunds());
    }
}
