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

}