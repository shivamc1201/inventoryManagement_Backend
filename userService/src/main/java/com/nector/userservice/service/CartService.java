package com.nector.userservice.service;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.dto.cart.PlaceOrderRequest;
import com.nector.userservice.exception.ActiveOrderExistsException;
import com.nector.userservice.exception.CartItemNotFoundException;
import com.nector.userservice.exception.CartNotFoundException;
import com.nector.userservice.exception.DistributorNotFoundException;
import com.nector.userservice.exception.InvalidCartStatusException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.*;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.dto.invoice.ProformaInvoice;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.service.SalesHierarchyValidationService;
import com.nector.userservice.service.PaymentService;
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import com.nector.userservice.ordertracking.repository.OrderTrackingStepRepository;
import com.nector.userservice.ordertracking.dto.UpdateStepRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final UserRepository userRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final SalesHierarchyValidationService salesHierarchyValidationService;
    private final OrderTrackingService orderTrackingService;
    private final OrderTrackingStepRepository orderTrackingStepRepository;
    private final HtmlToPdfService htmlToPdfService;
    private final @org.springframework.context.annotation.Lazy PaymentService paymentService;

    // Self-injection to enable @Async method calls through Spring proxy
    private CartService self;

    // Setter injection for self-reference to enable async proxy calls
    // @Lazy breaks the circular dependency
    @org.springframework.beans.factory.annotation.Autowired
    public void setSelf(@org.springframework.context.annotation.Lazy CartService cartService) {
        this.self = cartService;
    }


    @Transactional
    public CartResponse addItemsToCart(Long distributorId, List<AddToCartRequest> requests) {
        long cartStart = System.currentTimeMillis();
        Cart cart = getOrCreateActiveCart(distributorId);

        long itemsLoopStart = System.currentTimeMillis();
        int itemCount = 0;
        for (AddToCartRequest request : requests) {
            long itemStart = System.currentTimeMillis();
            itemCount++;
            try {
                long dbStart = System.currentTimeMillis();
                FinishedProduct finishedProduct = finishedProductRepository.findBySku(request.getItemId())
                        .filter(FinishedProduct::getActive)
                        .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));
                long existsStart = System.currentTimeMillis();
                if (!finishedProductRepository.existsById(finishedProduct.getId())) {
                    throw new ItemNotFoundException("Item with ID " + finishedProduct.getId() + " does not exist");
                }
                long cartItemStart = System.currentTimeMillis();
                Optional<CartItem> existingCartItem =
                        cartItemRepository.findByCartIdAndItemId(cart.getId(), finishedProduct.getId());
                long saveStart = System.currentTimeMillis();
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
                    cartItem.setUnitType(finishedProduct.getUnitName());
                    log.info("Setting cart item price: FinishedProduct price={}, CartItem priceAtTime={}",
                            finishedProduct.getPrice(), cartItem.getPriceAtTime());
                    cartItemRepository.save(cartItem);
                    cart.getCartItems().add(cartItem);
                }
            } catch (Exception e) {
                log.error("Failed to add item {} to cart for distributor {}: {}", request.getItemId(), distributorId, e.getMessage());
                throw e;
            }
        }
        long saveCartStart = System.currentTimeMillis();
        cart.setStatus(Cart.CartStatus.ACTIVE);
        Cart updatedCart = cartRepository.save(cart);
        
        // Initialize order tracking synchronously AFTER cart is saved
        // This ensures the cart exists in DB and transaction is committed
        // We run it sync to avoid transaction isolation issues with async
        initializeOrderTrackingForCart(updatedCart, distributorId);
        
        long mapStart = System.currentTimeMillis();
        CartResponse response = mapToResponse(updatedCart);
        return response;
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

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No placed cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getActiveCartByDistributorId(Long distributorId) {
        log.info("Fetching Active cart for distributor {}", distributorId);

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No Active cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getPlacedCartByDistributorId(Long distributorId) {
        log.info("Fetching Active cart for distributor {}", distributorId);

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No placed cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getDismissedCarts(Long distributorId) {
        List<Cart> dismissedCarts = cartRepository.findAllByDistributorIdAndStatus(distributorId, Cart.CartStatus.DISMISSED);
        return dismissedCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

        // Assign salesperson from distributor and validate against sales_person table
        distributorRepository.findById(distributorId).ifPresent(distributor -> {
            if (distributor.getSalespersonId() != null) {
                // Validate that salesperson exists in sales_person table
                SalesPerson salesperson = salesPersonRepository.findById(distributor.getSalespersonId())
                        .orElseThrow(() -> new RuntimeException("Salesperson with ID " + distributor.getSalespersonId() + " not found in sales_person table"));
                
                newCart.setSalespersonId(salesperson.getId());
                newCart.setSalespersonName(salesperson.getName());
                newCart.setDistributorName(distributor.getFirstName());
                
                log.info("Assigned salesperson {} (ID: {}) to cart for distributor {}", 
                        salesperson.getName(), salesperson.getId(), distributorId);
            }
        });

        return cartRepository.save(newCart);
    }

    public CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Fetch distributor and salesperson information
        if (cart.getDistributorId() != null) {
            response.setDistributorId(cart.getDistributorId());

            distributorRepository.findById(cart.getDistributorId()).ifPresent(distributor -> {
                response.setDistributorName(distributor.getFirstName());
                response.setDistributorAddress(distributor.getAddress());

                // Set salesperson information from distributor's salespersonId
                if (distributor.getSalespersonId() != null) {
                    response.setSalespersonId(distributor.getSalespersonId());

                    // Fetch salesperson name from SalesPerson table
                    salesPersonRepository.findById(distributor.getSalespersonId()).ifPresent(salesperson -> {
                        response.setSalespersonName(salesperson.getName());
                    });
                }
            });
        }

        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());

        response.setCartItems(cartItemResponses);

        // Set dismiss reason if present
        response.setDismissReason(cart.getDismissReason());

        // Set deliveryBy from cart, or fetch from order tracking step 1 if not present
        String deliveryBy = cart.getDeliveryBy();
        if (deliveryBy == null && cart.getStatus().ordinal() >= Cart.CartStatus.PLACED.ordinal()) {
            try {
                var orderTracking = orderTrackingService.getOrderRepository().findByCartId(cart.getId());
                if (orderTracking != null) {
                    var step1 = orderTrackingStepRepository
                        .findByOrderIdAndStepSequence(orderTracking.getId(), 1)
                        .orElse(null);
                    if (step1 != null) {
                        deliveryBy = step1.getDeliveryBy();
                        log.debug("Fetched deliveryBy '{}' from order tracking for cart {}", deliveryBy, cart.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch deliveryBy from order tracking for cart {}: {}", cart.getId(), e.getMessage());
            }
        }
        response.setDeliveryBy(deliveryBy);

        // Calculate total cart amount
        BigDecimal totalAmount = cartItemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCartAmount(totalAmount);
        
        // Calculate total weight and convert to volume in tons (assuming 1 ton = 1000 kg)
        BigDecimal totalWeight = cart.calculateTotalWeight();
        BigDecimal volumeInTons = totalWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
        response.setVolumeInTons(volumeInTons);
        response.setTotalCartWeightKg(totalWeight);

        return response;
    }


    @Transactional(readOnly = true)
    public List<CartResponse> getPendingApprovalCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getPlacedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getActiveCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CartResponse> getDismissedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.DISMISSED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CartResponse> getApprovedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.APPROVED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartResponse approveCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        // Check if cart is placed before approving
        if (cart.getStatus() != Cart.CartStatus.PLACED) {
            throw new InvalidCartStatusException("Cannot approve cart with status: " + cart.getStatus());
        }
        // Populate denormalized fields
        long denormStart = System.currentTimeMillis();
        distributorRepository.findById(cart.getDistributorId()).ifPresent(distributor -> {
            cart.setDistributorName(distributor.getFirstName());
            if (distributor.getSalespersonId() != null) {
                cart.setSalespersonId(distributor.getSalespersonId());
                salesPersonRepository.findById(distributor.getSalespersonId()).ifPresent(salesperson -> {
                    cart.setSalespersonName(salesperson.getName());
                });
            }
        });
        // Calculate and set total cart amount
        long calcStart = System.currentTimeMillis();
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalCartAmount(totalAmount);
        cart.setStatus(Cart.CartStatus.APPROVED);
        long saveStart = System.currentTimeMillis();
        Cart updatedCart = cartRepository.save(cart);
        // Update Order Tracking Steps async to not block the response
        if (self != null) {
            self.updateOrderTrackingAsync(updatedCart);
        } else {
            updateOrderTrackingSync(updatedCart);
        }
        // Generate Proforma Invoice async to not block the response
        if (self != null) {
            self.generateProformaInvoiceAsync(cartId, updatedCart.getId());
        } else {
            generateProformaInvoiceSync(cartId, updatedCart.getId());
        }
        // Use fast response mapper - avoid extra DB lookups
        long mapStart = System.currentTimeMillis();
        CartResponse response = mapToResponseFast(updatedCart);
        return response;
    }

    /**
     * Async wrapper to update order tracking without blocking the API response
     */
    @Async
    public void updateOrderTrackingAsync(Cart updatedCart) {
        long asyncStart = System.currentTimeMillis();
        try {
            updateOrderTrackingSync(updatedCart);
            log.info("[TIMING-ASYNC] Order tracking update completed in {} ms for cart {}",
                    System.currentTimeMillis() - asyncStart, updatedCart.getId());
        } catch (Exception e) {
            log.error("[TIMING-ASYNC] Order tracking update failed after {} ms for cart {}: {}",
                    System.currentTimeMillis() - asyncStart, updatedCart.getId(), e.getMessage());
        }
    }

    /**
     * Synchronous order tracking update - used as fallback or called async
     */
    private void updateOrderTrackingSync(Cart updatedCart) {
        // Update Order Tracking Step 3: Approved from Sales
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order =
                orderTrackingService.getOrderRepository().findByCartId(updatedCart.getId());

            // If order tracking doesn't exist, create it first
            if (order == null) {
                log.warn("Order tracking not found for cart {}, creating it now", updatedCart.getId());
                try {
                    paymentService.createOrderTrackingFromCart(updatedCart.getId());
                    // Fetch the newly created order tracking
                    order = orderTrackingService.getOrderRepository().findByCartId(updatedCart.getId());
                    if (order == null) {
                        log.error("Failed to create order tracking for cart {}", updatedCart.getId());
                        return;
                    }
                    log.info("Successfully created order tracking for cart {}", updatedCart.getId());
                } catch (Exception e) {
                    log.error("Error creating order tracking for cart {}: {}", updatedCart.getId(), e.getMessage(), e);
                    return;
                }
            }

            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus("completed");
                request.setRemarks("Order approved by sales team");
                request.setDate(java.time.LocalDate.now().toString());

                // Add assigned person (salesperson) information
                if (updatedCart.getSalespersonId() != null) {
                    request.setAssignedPersonId(updatedCart.getSalespersonId());
                    request.setAssignedPersonName(updatedCart.getSalespersonName());
                    request.setAssignedPersonRole("SALES_EXECUTIVE");

                    // Get salesperson details
                    salesPersonRepository.findById(updatedCart.getSalespersonId()).ifPresent(salesperson -> {
                        request.setAssignedPersonPhone(salesperson.getPhone());
                        request.setAssignedPersonEmail(salesperson.getEmail());
                    });
                }

                orderTrackingService.updateStepBySequence(order.getId(), 3, request);
                log.info("Order tracking Step 3 updated for cart {}", updatedCart.getId());

                // Also explicitly complete Step 2 (Pending Approval from Sales) if it was IN_PROGRESS
                UpdateStepRequest step2Request = new UpdateStepRequest();
                step2Request.setStatus("completed");
                step2Request.setRemarks("Sales approval completed");
                step2Request.setDate(java.time.LocalDate.now().toString());

                // Add assigned person (salesperson) information
                if (updatedCart.getSalespersonId() != null) {
                    step2Request.setAssignedPersonId(updatedCart.getSalespersonId());
                    step2Request.setAssignedPersonName(updatedCart.getSalespersonName());
                    step2Request.setAssignedPersonRole("SALES_EXECUTIVE");

                    // Get salesperson details
                    salesPersonRepository.findById(updatedCart.getSalespersonId()).ifPresent(salesperson -> {
                        step2Request.setAssignedPersonPhone(salesperson.getPhone());
                        step2Request.setAssignedPersonEmail(salesperson.getEmail());
                    });
                }

                try {
                    orderTrackingService.updateStepBySequence(order.getId(), 2, step2Request);
                    log.info("Order tracking Step 2 also completed for cart {}", updatedCart.getId());
                } catch (Exception e) {
                    // Step 2 might already be completed, log but don't fail
                    log.warn("Step 2 already completed or not found for cart {}: {}", updatedCart.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to update Order Tracking Step 3 for cart {}: {}",
                updatedCart.getId(), e.getMessage());
            // Continue without failing the approval
        }
    }

    /**
     * Async wrapper to generate proforma invoice without blocking the API response
     */
    @Async
    public void generateProformaInvoiceAsync(Long cartId, Long updatedCartId) {
        long asyncStart = System.currentTimeMillis();
        try {
            generateProformaInvoiceSync(cartId, updatedCartId);
            log.info("[TIMING-ASYNC] Proforma invoice generation completed in {} ms for cart {}",
                    System.currentTimeMillis() - asyncStart, cartId);
        } catch (Exception e) {
            log.error("[TIMING-ASYNC] Proforma invoice generation failed after {} ms for cart {}: {}",
                    System.currentTimeMillis() - asyncStart, cartId, e.getMessage());
        }
    }

    /**
     * Synchronous proforma invoice generation - used as fallback or called async
     * Also updates order tracking Step 4
     */
    private void generateProformaInvoiceSync(Long cartId, Long updatedCartId) {
        log.info("Starting proforma invoice generation for cart {}", cartId);
        
        // Generate Proforma Invoice after approval
        proformaInvoiceService.generateProformaInvoice(cartId);
        log.info("Proforma invoice generated successfully for cart {}", cartId);

        // Update order tracking Step 4: PI Generated - set to "completed"
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order =
                orderTrackingService.getOrderRepository().findByCartId(updatedCartId);

            // If order tracking doesn't exist, create it first
            if (order == null) {
                log.warn("Order tracking not found for cart {} during PI generation, creating it now", updatedCartId);
                try {
                    paymentService.createOrderTrackingFromCart(updatedCartId);
                    // Fetch the newly created order tracking
                    order = orderTrackingService.getOrderRepository().findByCartId(updatedCartId);
                    if (order == null) {
                        log.error("Failed to create order tracking for cart {} during PI generation", updatedCartId);
                        return;
                    }
                    log.info("Successfully created order tracking for cart {} during PI generation", updatedCartId);
                } catch (Exception e) {
                    log.error("Error creating order tracking for cart {} during PI generation: {}", 
                        updatedCartId, e.getMessage(), e);
                    return;
                }
            }

            if (order != null) {
                UpdateStepRequest step4Request = new UpdateStepRequest();
                step4Request.setStatus("completed");
                step4Request.setRemarks("Proforma Invoice generated successfully");
                step4Request.setDate(java.time.LocalDate.now().toString());
                step4Request.setHasDownload(true);
                step4Request.setDownloadLabel("Download Proforma Invoice");

                orderTrackingService.updateStepBySequence(order.getId(), 4, step4Request);
                log.info("Order tracking Step 4 set to completed for cart {} (order tracking ID: {})", 
                    updatedCartId, order.getId());
            }
        } catch (Exception e) {
            log.error("Failed to update Order Tracking Step 4 for cart {}: {}",
                updatedCartId, e.getMessage(), e);
            // Continue without failing the approval
        }
    }


    @Transactional
    public void dismissCart(Long cartId, String reason) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));

        cart.setStatus(Cart.CartStatus.DISMISSED);
        cart.setDismissReason(reason);
        cartRepository.save(cart);
        
        // Update Order Tracking Step 3: Cancelled (Sales Rejection)
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByCartId(cartId);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus("cancelled");
                request.setRemarks("Order rejected by sales: " + reason);
                request.setDate(java.time.LocalDate.now().toString());
                
                // Add assigned person (salesperson) information
                if (cart.getSalespersonId() != null) {
                    request.setAssignedPersonId(cart.getSalespersonId());
                    request.setAssignedPersonName(cart.getSalespersonName());
                    request.setAssignedPersonRole("SALES_EXECUTIVE");
                    
                    // Get salesperson details
                    salesPersonRepository.findById(cart.getSalespersonId()).ifPresent(salesperson -> {
                        request.setAssignedPersonPhone(salesperson.getPhone());
                        request.setAssignedPersonEmail(salesperson.getEmail());
                    });
                }
                
                orderTrackingService.updateStepBySequence(order.getId(), 3, request);
                log.info("Order tracking Step 3 cancelled for cart {} with reason: {}", cartId, reason);
            }
        } catch (Exception e) {
            log.error("Failed to cancel Order Tracking Step 3 for cart {}: {}", 
                cartId, e.getMessage());
            // Continue without failing the dismissal
        }
        
        log.info("Cart {} dismissed successfully with reason: {}", cartId, reason);
    }

    @Transactional
    public CartResponse placeOrder(Long distributorId, PlaceOrderRequest request) {
        // Check if distributor has any active orders that prevent new orders
        long validationStart = System.currentTimeMillis();
        List<Cart.CartStatus> activeOrderStatuses = List.of(
            Cart.CartStatus.CHECKED_OUT,
            Cart.CartStatus.APPROVED,
            Cart.CartStatus.PAYMENT_APPROVED,
            Cart.CartStatus.PLACED
        );
        List<Cart> activeOrders = cartRepository.findByDistributorIdAndStatusIn(distributorId, activeOrderStatuses);
        if (!activeOrders.isEmpty()) {
            Cart activeOrder = activeOrders.get(0);
            throw new ActiveOrderExistsException("Distributor already has an active order with status: " + 
                activeOrder.getStatus() + ". Cannot place new order until existing order is completed or dismissed.");
        }
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + request.getCartId() + " not found"));
        // Verify cart belongs to the distributor
        if (!cart.getDistributorId().equals(distributorId)) {
            throw new CartNotFoundException("Cart does not belong to distributor " + distributorId);
        }
        // Check if cart is active before placing order
        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new InvalidCartStatusException("Cannot place order for cart with status: " + cart.getStatus());
        }
        // Save the address and update status
        long cartSaveStart = System.currentTimeMillis();
        cart.setAddress(request.getAddress());
        cart.setDeliveryBy(request.getDeliveryBy());
        cart.setStatus(Cart.CartStatus.PLACED);
        Cart updatedCart = cartRepository.save(cart);
        
        // Create order tracking synchronously after cart is saved
        // This ensures proper transaction visibility
        createOrderTrackingSync(updatedCart);
        
        // Use lightweight response mapper - avoid extra DB lookups
        long mapStart = System.currentTimeMillis();
        CartResponse response = mapToResponseFast(updatedCart);
        return response;
    }

    /**
     * Async method to create OrderTracking record - runs in background
     */
    @Async
    public void createOrderTrackingAsync(Cart cart) {
        long asyncStart = System.currentTimeMillis();
        try {
            createOrderTrackingSync(cart);
            log.info("[TIMING-ASYNC] OrderTracking creation completed in {} ms for cart {}",
                    System.currentTimeMillis() - asyncStart, cart.getId());
        } catch (Exception e) {
            log.error("[TIMING-ASYNC] OrderTracking creation failed after {} ms for cart {}: {}",
                    System.currentTimeMillis() - asyncStart, cart.getId(), e.getMessage());
        }
    }

    /**
     * Synchronous OrderTracking creation - used as fallback or called async
     */
    private void createOrderTrackingSync(Cart cart) {
        try {
            // Check if OrderTracking already exists for this cart
            com.nector.userservice.ordertracking.entity.OrderTracking existingOrder = 
                orderTrackingService.getOrderRepository().findByCartId(cart.getId());
            
            if (existingOrder == null) {
                // Delegate to PaymentService which generates SO number properly
                paymentService.createOrderTrackingFromCart(cart.getId());
                log.info("OrderTracking record created for cart {}", cart.getId());
            } else {
                // Order tracking exists - update deliveryBy on first step if cart has deliveryBy
                if (cart.getDeliveryBy() != null) {
                    try {
                        com.nector.userservice.ordertracking.dto.UpdateStepRequest request = 
                            new com.nector.userservice.ordertracking.dto.UpdateStepRequest();
                        request.setDeliveryBy(cart.getDeliveryBy());
                        
                        orderTrackingService.updateStepBySequence(existingOrder.getId(), 1, request);
                        log.info("Updated deliveryBy '{}' for existing OrderTracking orderId={} cartId={}", 
                            cart.getDeliveryBy(), existingOrder.getId(), cart.getId());
                    } catch (Exception updateEx) {
                        log.error("Failed to update deliveryBy for existing OrderTracking orderId={} cartId={}: {}",
                            existingOrder.getId(), cart.getId(), updateEx.getMessage());
                    }
                } else {
                    log.info("OrderTracking record already exists for cart {}, skipping creation", cart.getId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create OrderTracking record for cart {}: {}", 
                cart.getId(), e.getMessage());
        }
    }

    /**
     * Fast response mapper - uses denormalized data from cart, avoids extra DB lookups
     * Skips distributor/salesperson lookups - uses data already stored in cart entity
     */
    private CartResponse mapToResponseFast(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        response.setDismissReason(cart.getDismissReason());

        // Use denormalized data from cart - no DB lookups
        if (cart.getDistributorId() != null) {
            response.setDistributorId(cart.getDistributorId());
            response.setDistributorName(cart.getDistributorName());
        }
        
        // Use denormalized salesperson data from cart
        response.setSalespersonId(cart.getSalespersonId());
        response.setSalespersonName(cart.getSalespersonName());

        // Map cart items efficiently
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());
        response.setCartItems(cartItemResponses);

        // Calculate total from pre-mapped items (avoid recalculating from DB)
        BigDecimal totalAmount = cartItemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCartAmount(totalAmount);
        
        // Calculate volume using cart's total weight method
        BigDecimal totalWeight = cart.calculateTotalWeight();
        BigDecimal volumeInTons = totalWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
        response.setVolumeInTons(volumeInTons);
        response.setTotalCartWeightKg(totalWeight);
        
        // Set deliveryBy from cart, or fetch from order tracking step 1 if not present
        String deliveryBy = cart.getDeliveryBy();
        if (deliveryBy == null && cart.getStatus().ordinal() >= Cart.CartStatus.PLACED.ordinal()) {
            try {
                var orderTracking = orderTrackingService.getOrderRepository().findByCartId(cart.getId());
                if (orderTracking != null) {
                    var step1 = orderTrackingStepRepository
                        .findByOrderIdAndStepSequence(orderTracking.getId(), 1)
                        .orElse(null);
                    if (step1 != null) {
                        deliveryBy = step1.getDeliveryBy();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch deliveryBy from order tracking for cart {}: {}", cart.getId(), e.getMessage());
            }
        }
        response.setDeliveryBy(deliveryBy);

        return response;
    }



    @Transactional
    public CartResponse editCart(Long cartId, List<AddToCartRequest> requests) {
        log.info("Editing cart {} with {} items", cartId, requests.size());

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));

        for (AddToCartRequest request : requests) {
            FinishedProduct finishedProduct = finishedProductRepository.findBySku(request.getItemId())
                    .filter(FinishedProduct::getActive)
                    .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));

            Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), finishedProduct.getId());

            if (existingCartItem.isPresent()) {
                CartItem cartItem = existingCartItem.get();
                cartItem.setQuantity(request.getQuantity());
                cartItemRepository.save(cartItem);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(finishedProduct);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setPriceAtTime(finishedProduct.getPrice());
                cartItem.setUnitType(finishedProduct.getUnitName());
                log.info("Setting cart item price (edit): FinishedProduct price={}, CartItem priceAtTime={}", 
                        finishedProduct.getPrice(), cartItem.getPriceAtTime());
                cartItemRepository.save(cartItem);
                cart.getCartItems().add(cartItem);
            }
        }

        Cart updatedCart = cartRepository.save(cart);
        log.info("Cart edited successfully");
        return mapToResponse(updatedCart);
    }

    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setItemId(cartItem.getItem().getSku());
        response.setItemName(cartItem.getItem().getName());
        response.setItemSku(cartItem.getItem().getSku());
        response.setQuantity(cartItem.getQuantity());
        response.setPriceAtTime(cartItem.getPriceAtTime());
        response.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        
        // Calculate total volume for this cart item
        BigDecimal itemVolume = calculateItemVolume(cartItem);
        response.setTotalVolume(itemVolume);
        
        log.info("Mapping cart item to response: Item={}, DB priceAtTime={}, Response priceAtTime={}, TotalPrice={}, TotalVolume={}", 
                cartItem.getItem().getName(), cartItem.getPriceAtTime(), response.getPriceAtTime(), response.getTotalPrice(), response.getTotalVolume());
        
        return response;
    }

    private BigDecimal calculateItemVolume(CartItem cartItem) {
        FinishedProduct product = cartItem.getItem();
        BigDecimal weight = product.getWeight();
        int quantity = cartItem.getQuantity();
        
        log.debug("Calculating volume for product: {}, weight: {}, unit: {}, quantity: {}", 
            product.getName(), weight, product.getUnit(), quantity);
        
        if (product.getUnit() == null || weight == null || weight.equals(BigDecimal.ZERO)) {
            log.warn("Product {} has null weight or unit, returning volume 0", product.getName());
            // Try to extract weight from product name as fallback (e.g., "50 Kg" from "MANKA MAHISHI 50 Kg")
            return extractWeightFromProductName(product.getName(), quantity);
        }
        
        switch (product.getUnit()) {
            case KG:
                // Convert KG to volume in tons (1 ton = 1000 kg)
                BigDecimal weightInKg = weight.multiply(BigDecimal.valueOf(quantity));
                return weightInKg.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
                
            case LITER:
                // Convert Liters to volume in tons (assuming density of 1 kg/liter for water)
                BigDecimal volumeInLiters = weight.multiply(BigDecimal.valueOf(quantity));
                return volumeInLiters.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
                
            case DOZEN:
                // For dozen, calculate weight per dozen and convert to volume in tons
                BigDecimal weightPerPiece = weight.divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalWeight = weightPerPiece.multiply(BigDecimal.valueOf(quantity));
                return totalWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
                
            case PIECES:
                // For pieces, calculate total weight and convert to volume in tons
                BigDecimal totalWeightPieces = weight.multiply(BigDecimal.valueOf(quantity));
                return totalWeightPieces.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
                
            default:
                // Fallback to simple calculation
                BigDecimal fallbackWeight = weight.multiply(BigDecimal.valueOf(quantity));
                return fallbackWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
        }
    }

    private BigDecimal extractWeightFromProductName(String productName, int quantity) {
        try {
            // Extract weight from product name (e.g., "50 Kg" from "MANKA MAHISHI 50 Kg")
            String[] words = productName.split(" ");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].matches("\\d+(\\.\\d+)?") && 
                    (words[i + 1].equalsIgnoreCase("Kg") || words[i + 1].equalsIgnoreCase("Kg"))) {
                    
                    BigDecimal weightPerUnit = new BigDecimal(words[i]);
                    BigDecimal totalWeight = weightPerUnit.multiply(BigDecimal.valueOf(quantity));
                    BigDecimal volumeInTons = totalWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
                    
                    log.info("Extracted weight {} from product name {}, total volume: {} tons", 
                        weightPerUnit, productName, volumeInTons);
                    return volumeInTons;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract weight from product name: {}", productName, e);
        }
        
        log.warn("Could not extract weight from product name: {}, returning 0", productName);
        return BigDecimal.ZERO;
    }

    public byte[] downloadProformaInvoice(Long cartId) {
        log.info("Downloading proforma invoice for cart ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        ProformaInvoice invoice = createInvoiceFromCart(cart);
        String html = generateSimpleHtmlInvoice(invoice);
        byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);

        log.info("Proforma invoice PDF generated successfully for cart: {}", cartId);
        return pdfBytes;
    }

    private ProformaInvoice createInvoiceFromCart(Cart cart) {
        ProformaInvoice invoice = new ProformaInvoice();

        invoice.setPiNumber("PI-" + cart.getId() + "-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        invoice.setPiDate(java.time.LocalDate.now());
        invoice.setModeOfPayment("Bank Transfer");
        invoice.setCompanyName("Your Company Name");
        invoice.setCompanyAddress("Your Company Address");
        invoice.setGstin("Your GSTIN");
        invoice.setContactNumber("+91-XXXXXXXXXX");
        invoice.setEmail("sales@yourcompany.com");

        List<com.nector.userservice.dto.invoice.InvoiceItem> items = java.util.stream.IntStream.range(0, cart.getCartItems().size())
                .mapToObj(i -> {
                    CartItem cartItem = cart.getCartItems().get(i);
                    com.nector.userservice.dto.invoice.InvoiceItem item = new com.nector.userservice.dto.invoice.InvoiceItem();
                    item.setSrNo(i + 1);
                    item.setDescription(cartItem.getItem().getName());
                    item.setHsnCode("1234");
                    item.setQuantity(cartItem.getQuantity());
                    item.setRatePerUnit(cartItem.getPriceAtTime().doubleValue());
                    item.setUnit("Pcs");
                    item.setAmount(cartItem.getPriceAtTime().doubleValue() * cartItem.getQuantity());
                    
                    // Set altQty with totalVolume from cart item
                    BigDecimal itemVolume = calculateItemVolume(cartItem);
                    item.setAltQty(itemVolume.toString());
                    
                    return item;
                })
                .toList();

        invoice.setItems(items);

        double subtotal = items.stream().mapToDouble(com.nector.userservice.dto.invoice.InvoiceItem::getAmount).sum();
        double cgst = subtotal * 0.09;
        double sgst = subtotal * 0.09;
        double grandTotal = subtotal + cgst + sgst;

        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(0.0);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountInWords(com.nector.userservice.util.NumberToWordsUtil.convertToWords(grandTotal));

        return invoice;
    }

    private String generateSimpleHtmlInvoice(ProformaInvoice invoice) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Proforma Invoice</title>");
        html.append("<style>body{font-family:Arial;margin:20px;}");
        html.append(".header{text-align:center;margin-bottom:30px;}");
        html.append("table{width:100%;border-collapse:collapse;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}</style></head><body>");

        html.append("<div class='header'><h2>PROFORMA INVOICE</h2>");
        html.append("<p>PI Number: ").append(invoice.getPiNumber()).append("</p>");
        html.append("<p>Date: ").append(invoice.getPiDate()).append("</p></div>");

        html.append("<table><tr><th>Sr No</th><th>Description</th><th>Quantity</th>");
        html.append("<th>Rate</th><th>Amount</th></tr>");

        for (com.nector.userservice.dto.invoice.InvoiceItem item : invoice.getItems()) {
            html.append("<tr><td>").append(item.getSrNo()).append("</td>");
            html.append("<td>").append(item.getDescription()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(item.getRatePerUnit()).append("</td>");
            html.append("<td>").append(item.getAmount()).append("</td></tr>");
        }

        html.append("</table>");
        html.append("<p>Subtotal: ").append(invoice.getSubtotal()).append("</p>");
        html.append("<p>CGST (9%): ").append(invoice.getCgst()).append("</p>");
        html.append("<p>SGST (9%): ").append(invoice.getSgst()).append("</p>");
        html.append("<p><strong>Grand Total: ").append(invoice.getGrandTotal()).append("</strong></p>");
        html.append("<p>Amount in words: ").append(invoice.getAmountInWords()).append("</p>");

        html.append("</body></html>");
        return html.toString();
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getApprovedCart() {
        log.info("Fetching all carts with APPROVED status");
        List<Cart> approvedCarts = cartRepository.findByStatus(Cart.CartStatus.APPROVED);
        return approvedCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public CartResponse mapToResponseWithDenormalizedData(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Use denormalized fields if available, otherwise fetch from related tables
        response.setDistributorId(cart.getDistributorId());

        // For approved carts, prioritize denormalized fields
        if (cart.getDistributorName() != null) {
            response.setDistributorName(cart.getDistributorName());
        } else {
            distributorRepository.findById(cart.getDistributorId()).ifPresent(distributor -> {
                response.setDistributorName(distributor.getFirstName());
            });
        }

        if (cart.getSalespersonId() != null) {
            response.setSalespersonId(cart.getSalespersonId());
            if (cart.getSalespersonName() != null) {
                response.setSalespersonName(cart.getSalespersonName());
            } else {
                salesPersonRepository.findById(cart.getSalespersonId()).ifPresent(salesperson -> {
                    response.setSalespersonName(salesperson.getName());
                });
            }
        }

        // Set dismiss reason if present
        response.setDismissReason(cart.getDismissReason());

        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());
        response.setCartItems(cartItemResponses);

        // Use denormalized total if available, otherwise calculate
        if (cart.getTotalCartAmount() != null) {
            response.setTotalCartAmount(cart.getTotalCartAmount());
        } else {
            BigDecimal totalAmount = cartItemResponses.stream()
                    .map(CartItemResponse::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalCartAmount(totalAmount);
        }
        
        // Calculate total weight and convert to volume in tons (assuming 1 ton = 1000 kg)
        BigDecimal totalWeight = cart.calculateTotalWeight();
        BigDecimal volumeInTons = totalWeight.divide(BigDecimal.valueOf(1000), 6, BigDecimal.ROUND_HALF_UP);
        response.setVolumeInTons(volumeInTons);
        response.setTotalCartWeightKg(totalWeight);
        
        // Set deliveryBy from cart, or fetch from order tracking step 1 if not present
        String deliveryBy = cart.getDeliveryBy();
        if (deliveryBy == null && cart.getStatus().ordinal() >= Cart.CartStatus.PLACED.ordinal()) {
            try {
                var orderTracking = orderTrackingService.getOrderRepository().findByCartId(cart.getId());
                if (orderTracking != null) {
                    var step1 = orderTrackingStepRepository
                        .findByOrderIdAndStepSequence(orderTracking.getId(), 1)
                        .orElse(null);
                    if (step1 != null) {
                        deliveryBy = step1.getDeliveryBy();
                    }
                }
            } catch (Exception e) {
                log.debug("Could not fetch deliveryBy from order tracking for cart {}: {}", cart.getId(), e.getMessage());
            }
        }
        response.setDeliveryBy(deliveryBy);

        return response;
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getCartsBySalespersonHierarchy(Long managerSalespersonId, Cart.CartStatus status) {
        log.info("Fetching carts for salesperson hierarchy under manager ID: {} with status: {}", managerSalespersonId, status);
        
        // Get all salespersons under this manager (including the manager themselves)
        List<Long> salespersonIds = salesHierarchyValidationService.getAllSubordinateIds(managerSalespersonId);
        salespersonIds.add(managerSalespersonId); // Include the manager
        
        log.info("Found {} salespersons in hierarchy for manager ID: {}", salespersonIds.size(), managerSalespersonId);
        
        // Get carts for these salespersons with the specified status
        List<Cart> carts = cartRepository.findBySalespersonIdInAndStatus(salespersonIds, status);
        
        return carts.stream()
                .map(this::mapToResponseWithDenormalizedData)
                .collect(Collectors.toList());
    }

    /**
     * Async wrapper to initialize order tracking without blocking the API response
     * This runs in a separate thread pool
     */
    @Async
    public void initializeOrderTrackingForCartAsync(Cart cart, Long distributorId) {
        long asyncStart = System.currentTimeMillis();
        try {
            initializeOrderTrackingForCart(cart, distributorId);
            log.info("[TIMING-ASYNC] Order tracking initialization completed in {} ms", 
                    System.currentTimeMillis() - asyncStart);
        } catch (Exception e) {
            log.error("[TIMING-ASYNC] Async order tracking initialization failed after {} ms: {}", 
                    System.currentTimeMillis() - asyncStart, e.getMessage());
        }
    }

    /**
     * Initialize order tracking for a cart when items are first added
     * This ensures distributor tracking starts from the very beginning
     */
    private void initializeOrderTrackingForCart(Cart cart, Long distributorId) {
        log.info("Initializing order tracking for cart {} with distributor {}", cart.getId(), distributorId);
        try {
            // Check if order tracking already exists for this cart
            if (orderTrackingService.getOrderRepository().findByCartId(cart.getId()) == null) {
                log.info("No existing order tracking found for cart {}, creating new one", cart.getId());
                // Delegate to PaymentService which handles SO number generation
                paymentService.createOrderTrackingFromCart(cart.getId());
                
                log.info("Order tracking initialized successfully for cart {} with distributor {}", 
                    cart.getId(), distributorId);
            } else {
                log.info("Order tracking already exists for cart {}, skipping initialization", cart.getId());
            }
        } catch (Exception e) {
            log.error("Failed to initialize order tracking for cart {}: {}", cart.getId(), e.getMessage(), e);
            // Don't fail the cart operation if tracking initialization fails
        }
    }
}
