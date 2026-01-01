package com.nector.userservice.service;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.exception.CartItemNotFoundException;
import com.nector.userservice.exception.CartNotFoundException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.Item;
import com.nector.userservice.repository.CartItemRepository;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.ItemRepository;
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
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;
    
    @Transactional
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item {} to cart for user {}", request.getItemId(), userId);
        
        Item item = itemRepository.findBySku(request.getItemId())
            .filter(Item::getActive)
            .orElseThrow(() -> new ItemNotFoundException(request.getItemId()));
        
        Cart cart = getOrCreateActiveCart(userId);
        
        Optional<CartItem> existingCartItem = cartItemRepository
            .findByCartIdAndItemId(cart.getId(), item.getId());
        
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Release old reserved stock and reserve new quantity
            inventoryService.releaseStock(item.getId(), cartItem.getQuantity());
            inventoryService.reserveStock(item.getId(), newQuantity);
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            inventoryService.reserveStock(item.getId(), request.getQuantity());
            
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtTime(item.getPrice());
            
            cartItemRepository.save(cartItem);
            cart.getCartItems().add(cartItem);
        }
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("Item added to cart successfully for user {}", userId);
        
        return mapToResponse(updatedCart);
    }
    

    @Transactional
    public CartResponse removeItemFromCart(Long cartItemId) {
        log.info("Removing cart item {}", cartItemId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CartItemNotFoundException(cartItemId));
        
        Cart cart = cartItem.getCart();
        
        // Release reserved stock
        inventoryService.releaseStock(cartItem.getItem().getId(), cartItem.getQuantity());
        
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
            .orElseThrow(() -> new CartNotFoundException(userId));
        
        return mapToResponse(cart);
    }
    
    private Cart getOrCreateActiveCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        Cart newCart = new Cart();
        newCart.setUserId(userId);
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