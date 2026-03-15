package com.nector.userservice.interceptors.order;

import com.nector.userservice.cloudinary.CloudinaryService;
import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.dto.cart.CartResponse;
import com.nector.userservice.model.ProformaInvoice;
import com.nector.userservice.repository.ProformaInvoiceRepository;
import com.nector.userservice.service.CartService;
import com.nector.userservice.service.ProformaInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Order Approval", description = "APIs for order approvals")
@Slf4j
public class OrderController {

    private final CartService cartService;
    private final ProformaInvoiceService proformaInvoiceService;
    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final CloudinaryStorageService cloudinaryStorageService;


    @GetMapping("/pending-order-approvals")
    @Operation(summary = "Get pending order approvals", description = "Retrieves all pending orders approval requests")
    @ApiResponse(responseCode = "200", description = "Pending order approvals retrieved successfully")
    public ResponseEntity<List<CartResponse>> getPendingOrders() {
        List<CartResponse> pendingCarts = cartService.getPendingApprovalCarts();
        return ResponseEntity.ok(pendingCarts);
    }

    @PutMapping("/approve/{cartId}")
    @Operation(summary = "Approve cart order", description = "Approves a cart order by changing its status to APPROVED")
    @ApiResponse(responseCode = "200", description = "Cart order approved successfully")
    public ResponseEntity<CartResponse> approveOrder(@PathVariable Long cartId) {
        CartResponse approvedCart = cartService.approveCart(cartId);
        return ResponseEntity.ok(approvedCart);
    }

    @GetMapping("/all-placed-carts")
    @Operation(summary = "Get placed carts", description = "Retrieves all carts with PLACED status")
    @ApiResponse(responseCode = "200", description = "Placed carts retrieved successfully")
    public ResponseEntity<List<CartResponse>> getPlacedCarts() {
        List<CartResponse> placedCarts = cartService.getPlacedCarts();
        return ResponseEntity.ok(placedCarts);
    }

    @GetMapping("/active-carts")
    @Operation(summary = "Get active carts", description = "Retrieves all carts with ACTIVE status")
    @ApiResponse(responseCode = "200", description = "Placed carts retrieved successfully")
    public ResponseEntity<List<CartResponse>> getActiveCarts() {
        List<CartResponse> activeCarts = cartService.getActiveCarts();
        return ResponseEntity.ok(activeCarts);
    }

    @GetMapping("/placed-carts")
    @Operation(summary = "Get placed carts", description = "Retrieves all carts with placed status")
    @ApiResponse(responseCode = "200", description = "Placed carts retrieved successfully")
    public ResponseEntity<List<CartResponse>> getplacedCarts() {
        List<CartResponse> placedCarts = cartService.getPlacedCarts();
        return ResponseEntity.ok(placedCarts);
    }

    // ==================== PROFORMA INVOICE APIS ====================

    @GetMapping("/proforma-invoice/{cartId}")
    @Operation(summary = "Get Proforma Invoice details", description = "Retrieves Proforma Invoice details for a given cart ID")
    @ApiResponse(responseCode = "200", description = "Proforma Invoice details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Proforma Invoice not found")
    public ResponseEntity<?> getProformaInvoice(@PathVariable Long cartId) {
        try {
            Map<String, Object> response = proformaInvoiceService.getProformaInvoiceDetails(cartId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve Proforma Invoice for cart ID: {} - {}", cartId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Proforma Invoice not found", "message", e.getMessage()));
        }
    }

    @GetMapping("/proforma-invoice/{cartId}/download")
    @Operation(summary = "Download Proforma Invoice PDF")
    @ApiResponse(
            responseCode = "200",
            description = "PDF downloaded",
            content = @Content(mediaType = "application/pdf")
    )
    public ResponseEntity<byte[]> downloadProformaInvoice(
            @PathVariable Long cartId) {

        try {

            byte[] pdfBytes =
                    proformaInvoiceService
                            .downloadProformaInvoicePdf(cartId);

            Map<String,Object> piDetails =
                    proformaInvoiceService
                            .getProformaInvoiceDetails(cartId);

            String piNumber =
                    (String) piDetails.get("piNumber");

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_PDF);

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(piNumber+".pdf")
                            .build()
            );

            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch(Exception e){

            log.error("Download failed",e);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @GetMapping("/proforma-invoice/{cartId}/url")
    @Operation(summary = "Get Proforma Invoice PDF URL", description = "Returns direct Cloudinary URL for PDF")
    @ApiResponse(responseCode = "200", description = "PDF URL retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Proforma Invoice or PDF URL not found")
    public ResponseEntity<?> getProformaInvoiceUrl(@PathVariable Long cartId) {
        try {
            Map<String, Object> response = proformaInvoiceService.getProformaInvoiceUrl(cartId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve PDF URL for cart ID: {} - {}", cartId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Proforma Invoice not found", "message", e.getMessage()));
        }
    }



    @GetMapping("/test-signed-url/{cartId}")
    public ResponseEntity<?> testSignedUrl(@PathVariable Long cartId) {
        try {
            com.nector.userservice.model.ProformaInvoice pi = proformaInvoiceRepository.findByCartId(cartId)
                    .orElseThrow(() -> new RuntimeException("PI not found"));

            // Extract public ID from URL
            String[] parts = pi.getPdfUrl().split("/");
            String publicId = parts[parts.length - 1];
            if (publicId.contains(".")) {
                publicId = publicId.substring(0, publicId.lastIndexOf('.'));
            }

            String signedUrl = cloudinaryStorageService.generateSignedUrl(publicId);

            return ResponseEntity.ok(Map.of(
                    "originalUrl", pi.getPdfUrl(),
                    "publicId", publicId,
                    "signedUrl", signedUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}