package com.nector.userservice.service;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.exception.CartItemNotFoundException;
import com.nector.userservice.exception.CartNotFoundException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.CartItemRepository;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final ProformaInvoiceService proformaInvoiceService;


    // TODO the dispatch team wil, check the real  quantity of the orders  while checking GDN


    @Transactional
    public CartResponse addItemsToCart(Long userId, Long distributorId, List<AddToCartRequest> requests) {
        log.info("Adding {} items to cart for user {} and distributor {}", requests.size(), userId, distributorId);
        Cart cart = getOrCreateActiveCart(userId, distributorId);
        
        for (AddToCartRequest request : requests) {
            log.info("Processing item {} for user {}", request.getItemId(), userId);
            try {
                FinishedProduct item = finishedProductRepository.findBySku(request.getItemId())
                        .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));

                Optional<CartItem> existingCartItem =
                        cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
                        
                if (existingCartItem.isPresent()) {
                    CartItem cartItem = existingCartItem.get();
                    int newQuantity = cartItem.getQuantity() + request.getQuantity();
                    cartItem.setQuantity(newQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    CartItem cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setItem(item);
                    cartItem.setQuantity(request.getQuantity());
                    cartItem.setPriceAtTime(item.getPrice());
                    cartItemRepository.save(cartItem);
                    cart.getCartItems().add(cartItem);
                }
            } catch (Exception e) {
                log.error("Failed to add item {} to cart for user {}: {}", request.getItemId(), userId, e.getMessage());
                throw e;
            }
        }
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("All items added to cart successfully for user {}", userId);
        return mapToResponse(updatedCart);
    }



    @Transactional
    public CartResponse removeItemFromCart(Long cartItemId) {
        log.info("Removing cart item {}", cartItemId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found"));
        
        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("Cart item removed successfully");
        
        return mapToResponse(updatedCart);
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        log.info("Fetching cart for user {}", userId);
        
        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseThrow(() -> new CartNotFoundException("No active cart found for user " + userId));
        
        return mapToResponse(cart);
    }
    
    private Cart getOrCreateActiveCart(Long userId, Long distributorId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setDistributorId(distributorId); // Update distributor if needed
            return cartRepository.save(cart);
        }
        
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setDistributorId(distributorId);
        newCart.setStatus(Cart.CartStatus.ACTIVE);
        
        return cartRepository.save(newCart);
    }
    
    private CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
            .map(this::mapCartItemToResponse)
            .collect(Collectors.toList());
        
        response.setCartItems(cartItemResponses);
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<CartResponse> getPendingApprovalCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        return pendingCarts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public CartResponse approveCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        cart.setStatus(Cart.CartStatus.APPROVED);
        Cart updatedCart = cartRepository.save(cart);
        
        // Generate Proforma Invoice after approval
        proformaInvoiceService.generateProformaInvoice(cartId);
        
        return mapToResponse(updatedCart);
    }
    
    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setItemId(cartItem.getItem().getId());
        response.setItemName(cartItem.getItem().getName());
        response.setItemSku(cartItem.getItem().getSku());
        response.setQuantity(cartItem.getQuantity());
        response.setPriceAtTime(cartItem.getPriceAtTime());
        response.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return response;
    }
}