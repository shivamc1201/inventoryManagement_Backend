package com.nector.userservice.transaction.controller;

import com.nector.userservice.transaction.dto.VoucherRequest;
import com.nector.userservice.transaction.dto.VoucherResponse;
import com.nector.userservice.transaction.service.TransactionVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction Voucher", description = "Create and list vouchers for the Transaction Master")
public class TransactionVoucherController {

    private final TransactionVoucherService voucherService;

    @PostMapping("/voucher")
    @Operation(summary = "Create a voucher")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    @GetMapping("/vouchers")
    @Operation(summary = "List vouchers, optionally filtered by ledger name")
    public ResponseEntity<List<VoucherResponse>> getVouchers(
            @RequestParam(value = "ledgerName", required = false) String ledgerName) {
        List<VoucherResponse> vouchers = ledgerName == null
                ? voucherService.getAllVouchers()
                : voucherService.getVouchersByLedgerName(ledgerName);
        return ResponseEntity.ok(vouchers);
    }

    @GetMapping("/voucher/{voucherNo}")
    @Operation(summary = "Get a single voucher by voucher number")
    public ResponseEntity<VoucherResponse> getVoucherByNo(@PathVariable String voucherNo) {
        return ResponseEntity.ok(voucherService.getVoucherByNo(voucherNo));
    }
}
