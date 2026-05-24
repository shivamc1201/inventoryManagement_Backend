package com.nector.userservice.bom.controller;

import com.nector.userservice.bom.dto.BomPriceChangeResponseDto;
import com.nector.userservice.bom.dto.PriceChangeActionDto;
import com.nector.userservice.bom.dto.RawMaterialPriceHistoryDto;
import com.nector.userservice.bom.service.BomPriceChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bom/price-changes")
@RequiredArgsConstructor
public class BomPriceChangeController {

    private final BomPriceChangeService bomPriceChangeService;

    /** All pending price change requests across all BOMs. */
    @GetMapping("/pending")
    public ResponseEntity<List<BomPriceChangeResponseDto>> getPendingChanges() {
        return ResponseEntity.ok(bomPriceChangeService.getPendingChanges());
    }

    /** Pending changes for a specific raw material. */
    @GetMapping("/pending/raw-material/{rawMaterialId}")
    public ResponseEntity<List<BomPriceChangeResponseDto>> getPendingByRawMaterial(
            @PathVariable Long rawMaterialId) {
        return ResponseEntity.ok(bomPriceChangeService.getPendingChangesByRawMaterial(rawMaterialId));
    }

    /** Pending changes for a specific BOM. */
    @GetMapping("/pending/bom/{bomId}")
    public ResponseEntity<List<BomPriceChangeResponseDto>> getPendingByBom(
            @PathVariable Long bomId) {
        return ResponseEntity.ok(bomPriceChangeService.getPendingChangesByBom(bomId));
    }

    /** Full audit trail — all requests regardless of status. */
    @GetMapping("/all")
    public ResponseEntity<List<BomPriceChangeResponseDto>> getAllChanges() {
        return ResponseEntity.ok(bomPriceChangeService.getAllChanges());
    }

    /** Price change history for a raw material (every price recorded over time). */
    @GetMapping("/history/{rawMaterialId}")
    public ResponseEntity<List<RawMaterialPriceHistoryDto>> getPriceHistory(
            @PathVariable Long rawMaterialId) {
        return ResponseEntity.ok(bomPriceChangeService.getPriceHistory(rawMaterialId));
    }

    // Admin approval endpoints are disabled — BOM prices now update automatically via FIFO
    // when a raw material price changes. Re-enable by uncommenting and restoring the
    // maker-checker block in BomPriceChangeServiceImpl.handleRawMaterialPriceChange.

    /*
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<BomPriceChangeResponseDto> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) PriceChangeActionDto action) {
        return ResponseEntity.ok(bomPriceChangeService.approve(requestId, action));
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<BomPriceChangeResponseDto> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) PriceChangeActionDto action) {
        return ResponseEntity.ok(bomPriceChangeService.reject(requestId, action));
    }

    @PutMapping("/bulk-approve")
    public ResponseEntity<List<BomPriceChangeResponseDto>> bulkApprove(
            @RequestParam Long rawMaterialId,
            @RequestBody(required = false) PriceChangeActionDto action) {
        return ResponseEntity.ok(bomPriceChangeService.bulkApprove(rawMaterialId, action));
    }

    @PutMapping("/bulk-reject")
    public ResponseEntity<List<BomPriceChangeResponseDto>> bulkReject(
            @RequestParam Long rawMaterialId,
            @RequestBody(required = false) PriceChangeActionDto action) {
        return ResponseEntity.ok(bomPriceChangeService.bulkReject(rawMaterialId, action));
    }
    */
}
