package com.nector.userservice.ordertracking.controller;

import com.nector.userservice.ordertracking.service.OrderDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/tracking")
@RequiredArgsConstructor
@Tag(name = "Order Documents", description = "APIs for downloading order documents (PI, GDN)")
@Slf4j
public class OrderDocumentController {

    private final OrderDocumentService docService;

    /**
     * GET /api/order/tracking/{id}/documents/PI
     * GET /api/order/tracking/{id}/documents/GDN
     */
    @GetMapping("/{id}/documents/{type}")
    @Operation(summary = "Download order document", description = "Downloads Proforma Invoice (PI) or Goods Dispatch Note (GDN)")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @PathVariable String type) {

        if (!type.equals("PI") && !type.equals("GDN")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] data = docService.getDocument(id, type);
            String filename = docService.getFilename(id, type);  // e.g. "PI-2026-001.pdf"

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(docService.getResource(id, type));

        } catch (RuntimeException ex) {
            log.error("Document not found for order {} type {}: {}", id, type, ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
