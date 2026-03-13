package com.nector.userservice.interceptors.order;


 import com.nector.userservice.dto.cart.CartResponse;
import com.nector.userservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Order Approval", description = "APIs for order approvals")
public class OrderController {

    private final CartService cartService;

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

}