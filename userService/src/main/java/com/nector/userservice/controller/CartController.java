package com.nector.userservice.controller;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItemToCart(
            @PathVariable Long userId, 
            @RequestParam Long distributorId,
            @Valid @RequestBody List<AddToCartRequest> requests) {
        log.info("Adding {} items to cart for user: {} and distributor: {}", requests.size(), userId, distributorId);
        try {
            CartResponse response = cartService.addItemsToCart(userId, distributorId, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error adding items to cart for user {}: {}", userId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }



    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeItemFromCart(@PathVariable Long cartItemId) {
        log.info("Removing cart item: {}", cartItemId);
        try {
            cartService.removeItemFromCart(cartItemId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removed from cart successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing cart item {}: {}", cartItemId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        log.info("Fetching cart for user: {}", userId);
        try {
            CartResponse response = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching cart for user {}: {}", userId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{cartId}/dismiss")
    public ResponseEntity<Map<String, String>> dismissCart(@PathVariable Long cartId) {
        log.info("Dismissing cart: {}", cartId);
        try {
            cartService.dismissCart(cartId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart dismissed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error dismissing cart {}: {}", cartId, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}