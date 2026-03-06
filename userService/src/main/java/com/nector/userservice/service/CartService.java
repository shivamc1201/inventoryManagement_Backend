package com.nector.userservice.service;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.exception.CartItemNotFoundException;
import com.nector.userservice.exception.CartNotFoundException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.model.Item;
import com.nector.userservice.repository.CartItemRepository;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.ItemRepository;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.interceptors.salesMapping.model.SalespersonDistributorMapping;
import com.nector.userservice.interceptors.salesMapping.model.MappingStatus;
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
    private final SalesMappingRepository salesMappingRepository;
    private final DistributorRepository distributorRepository;


    // TODO the dispatch team wil, check the real  quantity of the orders  while checking GDN


    @Transactional
    public CartResponse addItemsToCart(Long distributorId, List<AddToCartRequest> requests) {
        log.info("Adding {} items to cart for distributor {}", requests.size(), distributorId);
        Cart cart = getOrCreateActiveCart(distributorId);
        
        for (AddToCartRequest request : requests) {
            log.info("Processing item {} for distributor {}", request.getItemId(), distributorId);
            try {
                FinishedProduct finishedProduct = finishedProductRepository.findBySku(request.getItemId())
                        .filter(FinishedProduct::getActive)
                        .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));
                
                // Verify the finished product exists in database
                if (!finishedProductRepository.existsById(finishedProduct.getId())) {
                    throw new ItemNotFoundException("Item with ID " + finishedProduct.getId() + " does not exist");
                }

                Optional<CartItem> existingCartItem =
                        cartItemRepository.findByCartIdAndItemId(cart.getId(), finishedProduct.getId());
                        
                if (existingCartItem.isPresent()) {
                    CartItem cartItem = existingCartItem.get();
                    int newQuantity = cartItem.getQuantity() + request.getQuantity();
                    cartItem.setQuantity(newQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    CartItem cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setItem(finishedProduct);
                    cartItem.setQuantity(request.getQuantity());
                    cartItem.setPriceAtTime(finishedProduct.getPrice());
                    cartItemRepository.save(cartItem);
                    cart.getCartItems().add(cartItem);
                }
            } catch (Exception e) {
                log.error("Failed to add item {} to cart for distributor {}: {}", request.getItemId(), distributorId, e.getMessage());
                throw e;
            }
        }
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("All items added to cart successfully for distributor {}", distributorId);
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
    public CartResponse getCartByDistributorId(Long distributorId) {
        log.info("Fetching cart for distributor {}", distributorId);
        
        Cart cart = cartRepository.findActiveCartByDistributorId(distributorId)
            .orElseThrow(() -> new CartNotFoundException("No active cart found for distributor " + distributorId));
        
        return mapToResponse(cart);
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {
        log.info("Fetching cart by ID {}", cartId);
        
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        return mapToResponse(cart);
    }
    
    private Cart getOrCreateActiveCart(Long distributorId) {
        Optional<Cart> existingCart = cartRepository.findByDistributorIdAndStatus(distributorId, Cart.CartStatus.ACTIVE);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        Cart newCart = new Cart();
        newCart.setDistributorId(distributorId);
        newCart.setStatus(Cart.CartStatus.ACTIVE);
        
        return cartRepository.save(newCart);
    }
    
    private CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Fetch salesperson information for the distributor
        if (cart.getDistributorId() != null) {
            Optional<SalespersonDistributorMapping> mapping = salesMappingRepository
                .findByDistributorId(cart.getDistributorId())
                .stream()
                .filter(m -> m.getStatus() == MappingStatus.ACTIVE)
                .findFirst();
            
            if (mapping.isPresent()) {
                response.setSalespersonId(mapping.get().getSalespersonId());
                // You can add salesperson name lookup here if needed
                response.setSalespersonName("Salesperson-" + mapping.get().getSalespersonId());
            }
        }
        
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
            .map(this::mapCartItemToResponse)
            .collect(Collectors.toList());
        
        response.setCartItems(cartItemResponses);
        
        // Calculate total cart amount
        BigDecimal totalAmount = cartItemResponses.stream()
            .map(CartItemResponse::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCartAmount(totalAmount);
        
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
        
        // Check if cart is active before approval
        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new com.nector.userservice.exception.InvalidCartStatusException("Cannot approve cart with status: " + cart.getStatus());
        }
        
        cart.setStatus(Cart.CartStatus.APPROVED);
        Cart updatedCart = cartRepository.save(cart);
        
        // Generate Proforma Invoice after approval
        proformaInvoiceService.generateProformaInvoice(cartId);
        
        return mapToResponse(updatedCart);
    }
    
    @Transactional
    public void dismissCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        cart.setStatus(Cart.CartStatus.DISMISSED);
        cartRepository.save(cart);
        log.info("Cart {} dismissed successfully", cartId);
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