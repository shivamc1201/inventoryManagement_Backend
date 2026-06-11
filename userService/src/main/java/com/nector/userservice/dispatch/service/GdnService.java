package com.nector.userservice.dispatch.service;

import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.cloudinary.PdfGenerationResult;
import com.nector.userservice.dispatch.dto.*;
import com.nector.userservice.dispatch.entity.Gdn;
import com.nector.userservice.dispatch.entity.GdnItem;
import com.nector.userservice.dispatch.entity.InventoryVerification;
import com.nector.userservice.dispatch.entity.InventoryVerificationItem;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.dispatch.repository.InventoryVerificationRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.service.InventoryService;
import com.nector.userservice.service.HtmlToPdfService;
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import com.nector.userservice.ordertracking.repository.OrderTrackingStepRepository;
import com.nector.userservice.ordertracking.dto.UpdateStepRequest;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.service.RbacService;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.repository.DistributorLedgerRepository;
import com.nector.userservice.model.DistributorLedger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GdnService {
    
    private static final String DEFAULT_DISPATCH_ADDRESS = "Nectar Origin Private Limited\nPlot No 152/ 952, Salempur Saini , Khalgaon Barahat Bypass Road , Bhagalpur , Bihar -813222";
    
    private final GdnRepository gdnRepository;
    private final CartRepository cartRepository;
    private final InventoryVerificationRepository inventoryVerificationRepository;
    private final InventoryService inventoryService;
    private final HtmlToPdfService htmlToPdfService;
    private final TemplateEngine templateEngine;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final OrderTrackingService orderTrackingService;
    private final OrderTrackingStepRepository orderTrackingStepRepository;
    private final UserRepository userRepository;
    private final RbacService rbacService;
    private final DistributorRepository distributorRepository;
    private final DistributorLedgerRepository distributorLedgerRepository;
    private final com.nector.userservice.service.DocumentNumberService documentNumberService;
    private final com.nector.userservice.service.EmployeeKpiAssignmentService employeeKpiAssignmentService;
    
    /**
     * Get current user ID from security context
     * For now, returns hardcoded user ID 2L for logistics team
     * TODO: Replace with actual authentication context when available
     */
    private Long getCurrentUserId() {
        // Currently using hardcoded user ID for logistics team
        // When authentication is properly implemented, this should get the actual current user ID
        return 2L;
    }
    
    /**
     * Get current user details for assigning to order tracking steps
     */
    private void setCurrentUserDetails(UpdateStepRequest request) {
        Long currentUserId = getCurrentUserId();
        userRepository.findById(currentUserId).ifPresent(user -> {
            request.setAssignedPersonId(currentUserId);
            request.setAssignedPersonName(user.getFirstName() + " " + user.getLastName());
            request.setAssignedPersonEmail(user.getEmail());
            // Set phone if available, otherwise use default
            request.setAssignedPersonPhone(user.getContactNo() != null ? user.getContactNo() : "1800-LOGISTICS");
        });
    }
    
    @Transactional
    public GdnResponse generateGdn(Long orderId, GdnGenerationRequest request) {
        if (gdnRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("GDN already exists for order: " + orderId);
        }
        
        InventoryVerification inventoryVerification = inventoryVerificationRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    log.info("Inventory verification not found for order: {}. Performing verification now.", orderId);
                    try {
                        verifyInventoryForOrder(orderId);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot generate GDN: No existing inventory verification found, and failed to create new verification. Reason: " + e.getMessage());
                    }
                    return inventoryVerificationRepository.findByOrderId(orderId)
                            .orElseThrow(() -> new RuntimeException("Inventory verification failed to save for order: " + orderId));
                });

        // Fetch cart using the saved cartId from inventory verification
        Cart cart = cartRepository.findById(inventoryVerification.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found for cartId: " + inventoryVerification.getCartId()));

        log.info("Using cart ID {} for order {} to generate GDN", inventoryVerification.getCartId(), orderId);
        
        // Debug: Log all cart items with their product IDs
        log.info("Cart {} has {} items:", inventoryVerification.getCartId(), cart.getCartItems().size());
        cart.getCartItems().forEach(cartItem -> {
            if (cartItem.getItem() == null) {
                log.error("Cart item ID {} has NULL product!", cartItem.getId());
            } else {
                log.info("Cart item ID {} -> Product ID: {}, Product Name: '{}'", 
                        cartItem.getId(), cartItem.getItem().getId(), cartItem.getItem().getName());
            }
        });

//        if (!inventoryVerification.isCanProceed()) {
//            throw new RuntimeException("Cannot generate GDN. Inventory verification has failed for order: " + orderId);
//        }

        if (request == null) {
            request = new GdnGenerationRequest();
        }

        // If no verified items provided or if provided items are invalid, use all items from verification with available stock
        boolean hasValidRequestItems = false;
        if (request.getVerifiedItems() != null && !request.getVerifiedItems().isEmpty()) {
            hasValidRequestItems = request.getVerifiedItems().stream()
                .anyMatch(item -> item.getItemId() != null && item.getItemId() != 0);
        }
        
        if (!hasValidRequestItems) {
            log.info("No valid verified items provided in request, using items from inventory verification for order: {}", orderId);
            
            // Create a map of cart items for easy lookup
            Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
                    .collect(Collectors.toMap(cartItem -> cartItem.getItem().getId(), cartItem -> cartItem));
            
            request.setVerifiedItems(inventoryVerification.getItems().stream()
                .filter(verificationItem -> verificationItem.getAvailableQuantity() > 0)
                .map(verificationItem -> {
                    GdnGenerationRequest.InventoryVerificationItem item = new GdnGenerationRequest.InventoryVerificationItem();
                    item.setItemId(verificationItem.getItemId());
                    item.setOrderedQuantity(verificationItem.getOrderedQuantity());
                    item.setAvailableQuantity(verificationItem.getAvailableQuantity());
                    
                    // Get current cart item to ensure we have latest data
                    CartItem currentCartItem = cartItemMap.get(verificationItem.getItemId());
                    if (currentCartItem != null) {
                        log.debug("Using current cart data for item ID {}: quantity = {}", 
                                verificationItem.getItemId(), currentCartItem.getQuantity());
                    }
                    
                    item.setDispatchQuantity(Math.min(verificationItem.getOrderedQuantity(), verificationItem.getAvailableQuantity()));
                    return item;
                })
                .collect(Collectors.toList()));
        } else {
            log.info("Using provided verified items from request for order: {}", orderId);
            // Filter out any items with invalid IDs from the request
            request.setVerifiedItems(request.getVerifiedItems().stream()
                .filter(item -> item.getItemId() != null && item.getItemId() != 0)
                .collect(Collectors.toList()));
        }

        Gdn gdn = new Gdn();
        gdn.setGdnNumber(generateGdnNumber());
        gdn.setOrderId(orderId);
        gdn.setDispatchFromAddress(request.getDispatchFromAddress() != null ? 
            request.getDispatchFromAddress() : 
            DEFAULT_DISPATCH_ADDRESS);
        gdn.setShippingAddress(request.getShippingAddress());
        gdn.setVehicleNo(request.getVehicleNo());
        gdn.setTransportName(request.getTransportName());
        gdn.setDriverName(request.getDriverName());
        gdn.setDriverMobile(request.getDriverMobile());

        Map<Long, InventoryVerificationItem> verificationItemMap = inventoryVerification.getItems().stream()
                .collect(Collectors.toMap(InventoryVerificationItem::getItemId, item -> item));

        // Debug: Log all verification items
        log.info("Inventory verification {} has {} items:", inventoryVerification.getId(), inventoryVerification.getItems().size());
        inventoryVerification.getItems().forEach(verificationItem -> {
            log.info("Verification item -> Product ID: {}, Product Name: '{}', Ordered: {}, Available: {}", 
                    verificationItem.getItemId(), verificationItem.getItemName(), 
                    verificationItem.getOrderedQuantity(), verificationItem.getAvailableQuantity());
        });

        // Create a map of cart items for easy lookup
        Map<Long, CartItem> cartItemMap = cart.getCartItems().stream()
                .collect(Collectors.toMap(cartItem -> cartItem.getItem().getId(), cartItem -> cartItem));

        // Validate that no items have ID 0
        boolean hasInvalidItems = inventoryVerification.getItems().stream()
                .anyMatch(item -> item.getItemId() == null || item.getItemId() == 0);
        if (hasInvalidItems) {
            log.error("Inventory verification contains items with invalid ID 0 for order: {}", orderId);
            throw new RuntimeException("Inventory verification contains items with invalid ID 0. Please check your cart data.");
        }

        // Debug: Log request verified items
        log.info("Request has {} verified items:", request.getVerifiedItems().size());
        request.getVerifiedItems().forEach(verifiedItem -> {
            log.info("Request verified item -> Product ID: {}, Ordered: {}, Available: {}, Dispatch: {}", 
                    verifiedItem.getItemId(), verifiedItem.getOrderedQuantity(), 
                    verifiedItem.getAvailableQuantity(), verifiedItem.getDispatchQuantity());
        });

        List<GdnItem> gdnItems = request.getVerifiedItems().stream()
            .map(verifiedItem -> createGdnItemFromVerification(gdn, verifiedItem, verificationItemMap.get(verifiedItem.getItemId()), cartItemMap.get(verifiedItem.getItemId())))
            .collect(Collectors.toList());

        gdn.setGdnItems(gdnItems);
        gdn.setTotalPackages(gdnItems.stream().mapToInt(GdnItem::getNoOfUnitsDispatch).sum());
        gdn.setTotalWeight(gdnItems.stream()
            .map(item -> item.getWeightPerUnit().multiply(BigDecimal.valueOf(item.getNoOfUnitsDispatch())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Update stock using SKU-based lookup
        for (GdnGenerationRequest.InventoryVerificationItem verifiedItem : request.getVerifiedItems()) {
            // Get the cart item to find the SKU
            CartItem cartItem = cartItemMap.get(verifiedItem.getItemId());
            if (cartItem == null || cartItem.getItem() == null || cartItem.getItem().getSku() == null) {
                log.error("Cannot find SKU for item ID: {}", verifiedItem.getItemId());
                throw new RuntimeException("Cannot find SKU for item ID: " + verifiedItem.getItemId());
            }
            log.info("Updating stock for SKU: {}, quantity: {}", cartItem.getItem().getSku(), -verifiedItem.getDispatchQuantity());
            inventoryService.updateStockBySku(cartItem.getItem().getSku(), -verifiedItem.getDispatchQuantity());
        }

        Gdn savedGdn = gdnRepository.save(gdn);
        return mapToResponse(savedGdn);
    }
    
    @Transactional
    public GdnResponse generateSimpleGdn(Long orderId, SimpleGdnRequest request) {
        log.info("Generating simple GDN for order: {} - using live cart data", orderId);
        
        // Get cart directly using orderId (cartId = orderId in your system)
        Cart cart = cartRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Cart not found for order: " + orderId));
        
        log.info("Using live cart data - Cart {} has {} items", cart.getId(), cart.getCartItems().size());
        cart.getCartItems().forEach(cartItem -> {
            log.info("Live cart item -> Product ID: {}, Product Name: '{}', Quantity: {}", 
                    cartItem.getItem().getId(), cartItem.getItem().getName(), cartItem.getQuantity());
        });
        
        // Create GDN using live cart data
        return createGdnFromCart(orderId, cart, request);
    }
    
    @Transactional
    private GdnResponse createGdnFromCart(Long orderId, Cart cart, SimpleGdnRequest request) {
        if (gdnRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("GDN already exists for order: " + orderId);
        }
        
        if (cart.getStatus() != Cart.CartStatus.PAYMENT_APPROVED) {
            throw new RuntimeException("Order must be PAYMENT_APPROVED for GDN generation. Current status: " + cart.getStatus());
        }

        // Verify live stock availability for every cart item before touching anything
        List<String> stockErrors = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            String sku = cartItem.getItem().getSku();
            int ordered = cartItem.getQuantity();
            try {
                int available = inventoryService.getAvailableStockBySku(sku);
                if (available < ordered) {
                    stockErrors.add(String.format("'%s' (SKU: %s): ordered %d, available %d",
                            cartItem.getItem().getName(), sku, ordered, available));
                }
            } catch (Exception e) {
                stockErrors.add(String.format("'%s' (SKU: %s): not found in inventory",
                        cartItem.getItem().getName(), sku));
            }
        }
        if (!stockErrors.isEmpty()) {
            throw new RuntimeException("Insufficient stock for GDN generation. Please run inventory verification and resolve:\n" +
                    String.join("\n", stockErrors));
        }

        Gdn gdn = new Gdn();
        gdn.setGdnNumber(generateGdnNumber());
        gdn.setOrderId(orderId);
        gdn.setDispatchFromAddress(request.getDispatchFromAddress() != null ? 
            request.getDispatchFromAddress() : 
            DEFAULT_DISPATCH_ADDRESS);
        gdn.setShippingAddress(request.getShippingAddress());
        gdn.setVehicleNo(request.getVehicleNo());
        gdn.setTransportName(request.getTransportName());
        gdn.setDriverName(request.getDriverName());
        gdn.setDriverMobile(request.getDriverMobile());
        gdn.setDeliveryMethod(request.getDeliveryMethod());

        // Create GDN items directly from live cart data
        List<GdnItem> gdnItems = cart.getCartItems().stream()
            .map(cartItem -> {
                GdnItem gdnItem = new GdnItem();
                gdnItem.setGdn(gdn);
                gdnItem.setItemId(cartItem.getItem().getId());
                gdnItem.setItemDescription(cartItem.getItem().getName());
                gdnItem.setNoOfUnitsDispatch(cartItem.getQuantity());
                gdnItem.setWeightPerUnit(getWeightPerUnit(cartItem.getItem()));
                return gdnItem;
            })
            .collect(Collectors.toList());

        gdn.setGdnItems(gdnItems);
        gdn.setTotalPackages(gdnItems.stream().mapToInt(GdnItem::getNoOfUnitsDispatch).sum());
        gdn.setTotalWeight(gdnItems.stream()
            .map(item -> item.getWeightPerUnit().multiply(BigDecimal.valueOf(item.getNoOfUnitsDispatch())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Update stock based on live cart quantities using SKU-based lookup
        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getItem() == null || cartItem.getItem().getSku() == null) {
                log.error("Cart item ID {} has null item or SKU", cartItem.getId());
                throw new RuntimeException("Cart item ID " + cartItem.getId() + " has null item or SKU");
            }
            log.info("Updating stock for SKU: {}, quantity: {}", cartItem.getItem().getSku(), -cartItem.getQuantity());
            inventoryService.updateStockBySku(cartItem.getItem().getSku(), -cartItem.getQuantity());
        }

        Gdn savedGdn = gdnRepository.save(gdn);

        cart.setStatus(Cart.CartStatus.GDN_GENERATED);
        cartRepository.save(cart);

        // Auto-update Sales Target KPI for the salesperson
        try {
            if (cart.getSalespersonId() != null && cart.getTotalCartAmount() != null) {
                employeeKpiAssignmentService.autoUpdateKpiAchieved(
                        cart.getSalespersonId(),
                        "Sale Target",
                        cart.getTotalCartAmount()
                );
                log.info("Auto-updated 'Sale Target' KPI for salesperson {} by amount {}",
                        cart.getSalespersonId(), cart.getTotalCartAmount());
            }
        } catch (Exception e) {
            log.warn("Failed to auto-update 'Sale Target' KPI for salesperson {}: {}",
                    cart.getSalespersonId(), e.getMessage());
        }

        // Update Order Tracking Steps 8, 9, 10
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order =
                orderTrackingService.getOrderRepository().findByCartId(orderId);

            if (order != null) {
                // Step 8: Approved from Logistics -> Completed
                UpdateStepRequest step8Request = new UpdateStepRequest();
                step8Request.setStatus("completed");
                step8Request.setRemarks("Logistics approved - GDN generated: " + savedGdn.getGdnNumber());
                step8Request.setDate(java.time.LocalDate.now().toString());
                
                // Add assigned person (logistics team) information
                setCurrentUserDetails(step8Request);
                step8Request.setAssignedPersonRole("LOGISTICS_MANAGER");
                
                orderTrackingService.updateStepBySequence(order.getId(), 8, step8Request);
                
                // Step 9: GDN Generated -> Completed
                UpdateStepRequest step9Request = new UpdateStepRequest();
                step9Request.setStatus("completed");
                step9Request.setRemarks("GDN generated successfully: " + savedGdn.getGdnNumber());
                step9Request.setDate(java.time.LocalDate.now().toString());
                step9Request.setHasDownload(true);
                step9Request.setDownloadLabel("Download GDN");
                step9Request.setDocumentPath("/documents/gdn/" + savedGdn.getGdnNumber() + ".pdf");
                
                // Add assigned person (logistics team) information
                setCurrentUserDetails(step9Request);
                step9Request.setAssignedPersonRole("LOGISTICS_MANAGER");
                
                orderTrackingService.updateStepBySequence(order.getId(), 9, step9Request);
                
                // Step 10: Order is On the Way -> Completed
                UpdateStepRequest step10Request = new UpdateStepRequest();
                step10Request.setStatus("completed");
                step10Request.setRemarks("Order is on the way via " + request.getTransportName() + " (" + request.getVehicleNo() + ")");
                step10Request.setDate(java.time.LocalDate.now().toString());

                // Add assigned person (driver) information - use driver details for this step
                step10Request.setAssignedPersonName(request.getDriverName());
                step10Request.setAssignedPersonRole("DRIVER");
                step10Request.setAssignedPersonPhone(request.getDriverMobile());
                step10Request.setAssignedPersonEmail("driver@company.com"); // Generic driver email

                orderTrackingService.updateStepBySequence(order.getId(), 10, step10Request);

                log.info("Order tracking Steps 8, 9, 10 updated for order {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to update Order Tracking Steps 8-10 for order {}: {}",
                orderId, e.getMessage());
            // Continue without failing the GDN generation
        }

        PdfGenerationResult pdfResult = generateGdnPdf(savedGdn, cart);
        GdnResponse response = mapToResponse(savedGdn);
        if (pdfResult.isSuccess()) {
            response.setPdfGenerationStatus("SUCCESS");
            response.setPdfGenerationMessage("PDF generated and uploaded successfully");
        } else {
            response.setPdfGenerationStatus("FAILED");
            response.setPdfGenerationMessage(pdfResult.getMessage());
        }
        return response;
    }
    
    public InventoryVerificationResponse verifyInventoryForOrder(Long orderId) {
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
        if (cart.getStatus() != Cart.CartStatus.PAYMENT_APPROVED) {
            throw new RuntimeException("Order must be PAYMENT_APPROVED for inventory verification");
        }
        
        InventoryVerificationResponse response = new InventoryVerificationResponse();
        response.setOrderId(orderId);
        response.setOrderStatus(cart.getStatus().name());
        
        List<InventoryVerificationResponse.InventoryItem> items = cart.getCartItems().stream()
            .map(this::verifyItemInventory)
            .collect(Collectors.toList());
        
        response.setItems(items);
        
        boolean canProceed = items.stream().allMatch(InventoryVerificationResponse.InventoryItem::isSufficientStock);
        response.setCanProceedWithGdn(canProceed);

        if (canProceed) {
            response.setMessage("Inventory verified. All items have sufficient stock. Proceed with GDN generation.");
        } else {
            List<String> shortItems = items.stream()
                .filter(item -> !item.isSufficientStock())
                .map(item -> String.format("'%s' (SKU: %s): ordered %d, available %d",
                        item.getItemName(), item.getItemSku(), item.getOrderedQuantity(), item.getAvailableQuantity()))
                .collect(Collectors.toList());
            response.setMessage("Insufficient stock for the following items: " + String.join("; ", shortItems));
        }
        
        saveInventoryVerification(orderId, cart.getId(), items, canProceed);

        // Step 7: Awaiting Confirmation from Logistics -> Completed
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order =
                orderTrackingService.getOrderRepository().findByCartId(orderId);

            if (order != null) {
                UpdateStepRequest step7Request = new UpdateStepRequest();
                step7Request.setStatus("completed");
                step7Request.setRemarks("Logistics confirmation received - Inventory verified for GDN generation");
                step7Request.setDate(java.time.LocalDate.now().toString());

                // Add assigned person (logistics team) information
                setCurrentUserDetails(step7Request);
                step7Request.setAssignedPersonRole("LOGISTICS_MANAGER");

                orderTrackingService.updateStepBySequence(order.getId(), 7, step7Request);
                log.info("Order tracking Step 7 completed for order {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to update Order Tracking Step 7 for order {}: {}",
                orderId, e.getMessage());
            // Continue without failing the verification
        }

        return response;
    }

    public GdnResponse getGdnByOrderId(Long orderId) {
        Gdn gdn = gdnRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("GDN not found for order: " + orderId));
        return mapToResponse(gdn);
    }
    
    public Cart getCartByOrderId(Long orderId) {
        InventoryVerification inventoryVerification = inventoryVerificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Inventory verification not found for order: " + orderId));
        
        return cartRepository.findById(inventoryVerification.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found for cartId: " + inventoryVerification.getCartId()));
    }
    
    private String generateGdnNumber() {
        // Generate GDN number in format: DC/YYYY-YY/NNNN (e.g., DC/2026-27/0001)
        return documentNumberService.generateGdnNumber();
    }
    
    private GdnItem createGdnItemFromVerification(Gdn gdn, GdnGenerationRequest.InventoryVerificationItem verifiedItem, InventoryVerificationItem dbItem, CartItem cartItem) {
        log.debug("Creating GDN item for product ID: {}, dbItem: {}, cartItem: {}", 
                 verifiedItem.getItemId(), 
                 dbItem != null ? "present" : "NULL", 
                 cartItem != null ? "present" : "NULL");
        
        if (dbItem == null) {
            log.error("Item with ID {} not found in original inventory verification.", verifiedItem.getItemId());
            throw new RuntimeException("Item with ID " + verifiedItem.getItemId() + " not found in original inventory verification.");
        }
            
        if (verifiedItem.getDispatchQuantity() == null || verifiedItem.getDispatchQuantity() <= 0) {
            throw new RuntimeException("Invalid dispatch quantity for item: " + verifiedItem.getItemId());
        }
        if (verifiedItem.getDispatchQuantity() > dbItem.getAvailableQuantity()) {
            throw new RuntimeException("Dispatch quantity " + verifiedItem.getDispatchQuantity() + " for item " + verifiedItem.getItemId() + " exceeds available stock " + dbItem.getAvailableQuantity());
        }
            
        GdnItem gdnItem = new GdnItem();
        gdnItem.setGdn(gdn);
        gdnItem.setItemId(verifiedItem.getItemId());
        
        // Use cart item data if available, otherwise fall back to verification data
        if (cartItem != null && cartItem.getItem() != null) {
            gdnItem.setItemDescription(cartItem.getItem().getName());
            log.debug("Using cart item name for item ID {}: {}", verifiedItem.getItemId(), cartItem.getItem().getName());
        } else {
            gdnItem.setItemDescription(dbItem.getItemName());
            log.debug("Using verification item name for item ID {}: {}", verifiedItem.getItemId(), dbItem.getItemName());
        }
        
        gdnItem.setNoOfUnitsDispatch(verifiedItem.getDispatchQuantity());
        gdnItem.setWeightPerUnit(getWeightPerUnit(cartItem != null ? cartItem.getItem() : null, dbItem != null ? dbItem.getItemName() : null));
        return gdnItem;
    }
    
    private InventoryVerificationResponse.InventoryItem verifyItemInventory(CartItem cartItem) {
        InventoryVerificationResponse.InventoryItem item = new InventoryVerificationResponse.InventoryItem();


        // Debug logging to identify the issue
        if (cartItem.getItem() == null) {
            log.error("Cart item has null product for cart item ID: {}", cartItem.getId());
            throw new RuntimeException("Cart item has null product for cart item ID: " + cartItem.getId());
        }
        
        Long itemId = cartItem.getItem().getId();
        if (itemId == null || itemId == 0) {
            log.error("Product has invalid ID: {} for cart item ID: {}, product name: {}", 
                     itemId, cartItem.getId(), cartItem.getItem().getName());
            throw new RuntimeException("Product has invalid ID: " + itemId + " for cart item ID: " + cartItem.getId());
        }
        
        log.debug("Processing cart item ID: {}, product ID: {}, product name: {}", 
                 cartItem.getId(), itemId, cartItem.getItem().getName());
        
        item.setItemId(itemId);
        item.setItemName(cartItem.getItem().getName());
        item.setItemSku(cartItem.getItem().getSku());
        item.setOrderedQuantity(cartItem.getQuantity());

        // Get current stock from inventory service using SKU only
        Integer availableStock;
        try {
            availableStock = inventoryService.getAvailableStockBySku(cartItem.getItem().getSku());
            log.info("Successfully retrieved stock for SKU: {}, available quantity: {}", 
                    cartItem.getItem().getSku(), availableStock);
        } catch (Exception e) {
            log.error("Failed to get stock by SKU: {}. Item may not exist in inventory. Error: {}", 
                     cartItem.getItem().getSku(), e.getMessage());
            throw new RuntimeException("Item with SKU '" + cartItem.getItem().getSku() + 
                                       "' not found in inventory. Please ensure the item exists before generating GDN.");
        }
        item.setAvailableQuantity(availableStock);
        item.setSufficientStock(availableStock >= cartItem.getQuantity());
        
        if (availableStock >= cartItem.getQuantity()) {
            item.setStatus("SUFFICIENT_STOCK");
        } else if (availableStock > 0) {
            item.setStatus("PARTIAL_STOCK");
        } else {
            item.setStatus("OUT_OF_STOCK");
        }
        
        return item;
    }
    
    private GdnResponse mapToResponse(Gdn gdn) {
        GdnResponse response = new GdnResponse();
        response.setId(gdn.getId());
        response.setGdnNumber(gdn.getGdnNumber());
        response.setOrderId(gdn.getOrderId());
        response.setGdnDate(gdn.getGdnDate());
        response.setDispatchFromAddress(gdn.getDispatchFromAddress());
        response.setShippingAddress(gdn.getShippingAddress());
        response.setDeliveryMethod(gdn.getDeliveryMethod());
        response.setVehicleNo(gdn.getVehicleNo());
        response.setTransportName(gdn.getTransportName());
        response.setDriverName(gdn.getDriverName());
        response.setDriverMobile(gdn.getDriverMobile());
        response.setTotalWeight(gdn.getTotalWeight());
        response.setTotalPackages(gdn.getTotalPackages());
        response.setPdfUrl(gdn.getPdfUrl());
        
        List<GdnItemResponse> itemResponses = gdn.getGdnItems().stream()
            .map(this::mapItemToResponse)
            .collect(Collectors.toList());
        response.setGdnItems(itemResponses);
        
        return response;
    }
    
    private GdnItemResponse mapItemToResponse(GdnItem gdnItem) {
        GdnItemResponse response = new GdnItemResponse();
        response.setId(gdnItem.getId());
        response.setItemId(gdnItem.getItemId());
        response.setItemDescription(gdnItem.getItemDescription());
        response.setUnitType(gdnItem.getUnitType());
        response.setNoOfUnitsDispatch(gdnItem.getNoOfUnitsDispatch());
        response.setWeightPerUnit(gdnItem.getWeightPerUnit());
        return response;
    }

    private void saveInventoryVerification(Long orderId, Long cartId, List<InventoryVerificationResponse.InventoryItem> items, boolean canProceed) {
        InventoryVerification verification = inventoryVerificationRepository.findByOrderId(orderId)
                .orElse(new InventoryVerification());

        if (verification.getId() == null) {
            verification.setOrderId(orderId);
        }
        verification.setCartId(cartId);

        verification.setVerificationDate(LocalDateTime.now());
        verification.setStatus("ITEMS_VERIFIED");
        verification.setCanProceed(canProceed);

        List<InventoryVerificationItem> verificationItems = items.stream()
                .map(item -> {
                    // Validate item ID before creating verification item
                    if (item.getItemId() == null || item.getItemId() == 0) {
                        log.error("Attempted to save verification item with invalid ID: {}, item name: {}", 
                                 item.getItemId(), item.getItemName());
                        throw new RuntimeException("Cannot save verification item with invalid ID: " + item.getItemId());
                    }
                    
                    InventoryVerificationItem entityItem = new InventoryVerificationItem();
                    entityItem.setItemId(item.getItemId());
                    entityItem.setItemName(item.getItemName());
                    entityItem.setItemSku(item.getItemSku());
                    entityItem.setOrderedQuantity(item.getOrderedQuantity());
                    entityItem.setAvailableQuantity(item.getAvailableQuantity());
                    entityItem.setStockStatus(item.getStatus());
                    entityItem.setInventoryVerification(verification);
                    
                    log.debug("Creating verification item for product ID: {}, name: {}", 
                             item.getItemId(), item.getItemName());
                    
                    return entityItem;
                })
                .collect(Collectors.toList());

        if (verification.getItems() == null) {
            verification.setItems(verificationItems);
        } else {
            verification.getItems().clear();
            verification.getItems().addAll(verificationItems);
        }

        inventoryVerificationRepository.save(verification);
    }
    

    private Map<String, Object> createGdnDataForTemplate(Gdn gdn, Cart cart) {
        Map<String, Object> data = new HashMap<>();
        
        // GDN details
        data.put("gdnNumber", gdn.getGdnNumber());
        data.put("gdnDate", gdn.getGdnDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        data.put("orderId", gdn.getOrderId());
        
        // Address details
        data.put("dispatchFromAddress", gdn.getDispatchFromAddress());
        data.put("shippingAddress", gdn.getShippingAddress());

        
        // Distributor details
        String distributorName = "N/A";
        String distributorAddress = "N/A";
        String distributorContact = "N/A";
        
        if (cart.getDistributorId() != null) {
            try {
                Distributor distributor = distributorRepository.findById(cart.getDistributorId()).orElse(null);
                if (distributor != null) {
                    distributorName = distributor.getFirstName() + " " + distributor.getLastName();
                    distributorAddress = distributor.getAddress() != null ? distributor.getAddress() : "N/A";
                    distributorContact = distributor.getPhoneNumber() != null ? distributor.getPhoneNumber() : "N/A";
                    log.info("Found distributor for GDN: {} - {}", distributorName, distributorAddress);
                } else {
                    log.warn("Distributor not found for ID: {} in cart: {}", cart.getDistributorId(), cart.getId());
                }
            } catch (Exception e) {
                log.error("Error fetching distributor data for cart {}: {}", cart.getId(), e.getMessage());
            }
        } else {
            log.warn("No distributor ID found in cart: {}", cart.getId());
        }
        
        // Use cart distributor name as fallback
        if ("N/A".equals(distributorName) && cart.getDistributorName() != null) {
            distributorName = cart.getDistributorName();
        }
        
        data.put("distributorName", distributorName);
        data.put("distributorAddress", distributorAddress);
        data.put("distributorContact", distributorContact);
        
        // Transport details
        data.put("deliveryMethod",gdn.getDeliveryMethod());
        data.put("vehicleNo", gdn.getVehicleNo());
        data.put("transportName", gdn.getTransportName());
        data.put("driverName", gdn.getDriverName());
        data.put("driverMobile", gdn.getDriverMobile());
        
        // Items
        List<Map<String, Object>> items = gdn.getGdnItems().stream()
            .map(gdnItem -> {
                Map<String, Object> item = new HashMap<>();
                item.put("srNo", gdn.getGdnItems().indexOf(gdnItem) + 1);
                item.put("description", gdnItem.getItemDescription());
                item.put("quantity", gdnItem.getNoOfUnitsDispatch());
                item.put("unit", gdnItem.getUnitType());
                item.put("weight", gdnItem.getWeightPerUnit());
                return item;
            })
            .collect(Collectors.toList());
        data.put("items", items);
        
        // Totals
        data.put("totalPackages", gdn.getTotalPackages());
        data.put("totalWeight", gdn.getTotalWeight());
        
        // Company details (similar to invoice)
        data.put("companyName", "Nectar Origin Private Limited");
        data.put("companyAddress", "360 K, Shiv Parwati Nagar, Block Road No-2, Ward No 16, Kahalgaon, Bhagalpur Bihar, Bihar - 813203, India");
        data.put("contactNumber", "06429-450126,9797979522");
        data.put("logoPath", htmlToPdfService.getLogoDataUri());
        
        return data;
    }
    
    private void saveGdnPdfToFile(byte[] pdfBytes, String gdnNumber) {
        try {
            File dir = new File("gdns");
            if (!dir.exists()) dir.mkdirs();
            
            // Replace slashes with dashes for valid filename
            String fileName = gdnNumber.replace("/", "-") + ".pdf";
            String filePath = "gdns/" + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
            log.info("GDN PDF saved to: {}", new File(filePath).getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save GDN PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to save GDN PDF", e);
        }
    }



    // Update generateGdnPdf method
    public PdfGenerationResult generateGdnPdf(Gdn gdn, Cart cart) {
        log.info("=== GDN PDF GENERATION STARTED ===");
        log.info("GDN details - Number: {}, Order ID: {}, Timestamp: {}",
                gdn.getGdnNumber(), gdn.getOrderId(), java.time.LocalDateTime.now());

        String cloudinaryUrl = null;
        byte[] pdfBytes = null;
        try {
            // Step 1: Generate GDN data for template
            log.info("Step 1/4: Generating GDN data for template");
            Map<String, Object> gdnData = createGdnDataForTemplate(gdn, cart);
            log.info("GDN data created - Number: {}, Items: {}",
                    gdn.getGdnNumber(), gdn.getGdnItems().size());

            // Step 2: Generate HTML from template
            log.info("Step 2/4: Converting GDN to HTML template");
            Context context = new Context();
            context.setVariables(gdnData);
            String html = templateEngine.process("gdn", context);
            log.info("HTML generated successfully - Length: {} characters", html.length());

            // Step 3: Convert to PDF
            log.info("Step 3/4: Converting HTML to PDF bytes");
            pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
            log.info("PDF generated successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            // Step 4: Upload to Cloudinary
            log.info("Step 4/4: Uploading PDF to Cloudinary");
            cloudinaryUrl = uploadGdnPdfToCloudinary(pdfBytes, gdn.getGdnNumber());

            // Update GDN entity with Cloudinary URL
            gdn.setPdfUrl(cloudinaryUrl);
            gdnRepository.save(gdn);

            log.info("=== GDN PDF GENERATION COMPLETED ===");
            log.info("Success - GDN Number: {}, Cloudinary URL: {}, PDF Size: {} KB",
                    gdn.getGdnNumber(), cloudinaryUrl, pdfBytes.length / 1024.0);
            log.info("Entity updated - ID: {}, PDF URL stored: {}", gdn.getId(), gdn.getPdfUrl());

        } catch (Exception e) {
            log.error("=== GDN PDF GENERATION FAILED ===");
            log.error("Failure details - GDN Number: {}, Error: {}, Timestamp: {}",
                    gdn.getGdnNumber(), e.getMessage(), java.time.LocalDateTime.now());
            log.error("Stack trace:", e);
            return PdfGenerationResult.failure("PDF generation failed: " + e.getMessage());
        }
        return PdfGenerationResult.success(cloudinaryUrl, pdfBytes.length);
    }

    private String uploadGdnPdfToCloudinary(byte[] pdfBytes, String gdnNumber) {
        File tempFile = null;
        try {
            // Create temporary file
            tempFile = createTempGdnPdfFile(pdfBytes, gdnNumber);

            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryStorageService.uploadPdf(tempFile);

            log.info("GDN PDF uploaded to Cloudinary: {} -> {}", gdnNumber, cloudinaryUrl);
            return cloudinaryUrl;

        } catch (Exception e) {
            log.error("Failed to upload GDN PDF to Cloudinary: {} - {}", gdnNumber, e.getMessage());
            throw new RuntimeException("GDN PDF upload to Cloudinary failed", e);
        } finally {
            // Always cleanup temporary file
            cleanupTempGdnFile(tempFile);
        }
    }

    private File createTempGdnPdfFile(byte[] pdfBytes, String gdnNumber) throws IOException {
        // Create temp directory if not exists
        Path tempDir = Path.of("temp");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Create temp file
        String tempFileName = gdnNumber.replace("/", "-") + "_" + System.currentTimeMillis() + ".pdf";
        File tempFile = tempDir.resolve(tempFileName).toFile();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }

        log.debug("Created temporary GDN PDF file: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    private void cleanupTempGdnFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            try {
                Files.deleteIfExists(tempFile.toPath());
                log.debug("Cleaned up temporary GDN file: {}", tempFile.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to cleanup temporary GDN file: {} - {}",
                        tempFile.getAbsolutePath(), e.getMessage());
            }
        }
    }


    /**
     * Download GDN PDF from Cloudinary
     */
    public byte[] downloadGdnPdf(Long orderId) {
        log.info("=== DOWNLOAD GDN PDF ===");
        log.info("Download request - Order ID: {}, Timestamp: {}", orderId, java.time.LocalDateTime.now());

        try {
            Gdn gdn = gdnRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("GDN not found for order ID: " + orderId));

            log.info("GDN found - ID: {}, GDN Number: {}", gdn.getId(), gdn.getGdnNumber());

            // Check if PDF URL exists
            if (gdn.getPdfUrl() == null || gdn.getPdfUrl().isEmpty()) {
                log.error("PDF URL not available for GDN ID: {}", gdn.getId());
                throw new RuntimeException("PDF not available for GDN: " + gdn.getGdnNumber());
            }

            log.info("PDF URL found: {}", gdn.getPdfUrl());

            // Download using Cloudinary's URL download method
            byte[] pdfBytes = cloudinaryStorageService.downloadPdfByUrl(gdn.getPdfUrl());

            log.info("PDF downloaded successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            return pdfBytes;

        } catch (Exception e) {
            log.error("Failed to download PDF from Cloudinary for order ID: {} - {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to download PDF", e);
        }
    }

    /**
     * Get GDN PDF URL only
     */
    public Map<String, Object> getGdnUrl(Long orderId) {
        log.info("=== GET GDN PDF URL ===");
        log.info("URL request - Order ID: {}, Timestamp: {}", orderId, java.time.LocalDateTime.now());

        try {
            Gdn gdn = gdnRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("GDN not found for order ID: " + orderId));

            if (gdn.getPdfUrl() == null || gdn.getPdfUrl().isEmpty()) {
                log.error("PDF URL not available for GDN ID: {}", gdn.getId());
                throw new RuntimeException("PDF URL not available for GDN: " + gdn.getGdnNumber());
            }

            log.info("PDF URL retrieved successfully - Order ID: {}, GDN Number: {}, URL: {}",
                    orderId, gdn.getGdnNumber(), gdn.getPdfUrl());

            Map<String, Object> response = Map.of(
                    "orderId", orderId,
                    "gdnNumber", gdn.getGdnNumber(),
                    "pdfUrl", gdn.getPdfUrl(),
                    "totalPackages", gdn.getTotalPackages(),
                    "totalWeight", gdn.getTotalWeight()
            );

            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve PDF URL for order ID: {} - {}", orderId, e.getMessage());
            throw new RuntimeException("GDN not found", e);
        }
    }

    /**
     * Get all GDNs
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllGdns() {
        log.info("=== GET ALL GDNS ===");
        log.info("Request timestamp: {}", java.time.LocalDateTime.now());

        try {
            List<Gdn> gdns = gdnRepository.findAll();

            List<Map<String, Object>> response = gdns.stream()
                    .map(gdn -> {
                        Map<String, Object> gdnMap = new HashMap<>();
                        gdnMap.put("id", gdn.getId());
                        gdnMap.put("gdnNumber", gdn.getGdnNumber());
                        gdnMap.put("orderId", gdn.getOrderId());
                        gdnMap.put("gdnDate", gdn.getGdnDate());
                        gdnMap.put("dispatchFromAddress", gdn.getDispatchFromAddress());
                        gdnMap.put("shippingAddress", gdn.getShippingAddress());
                        gdnMap.put("deliveryMethod", gdn.getDeliveryMethod());
                        gdnMap.put("vehicleNo", gdn.getVehicleNo());
                        gdnMap.put("transportName", gdn.getTransportName());
                        gdnMap.put("driverName", gdn.getDriverName());
                        gdnMap.put("driverMobile", gdn.getDriverMobile());
                        gdnMap.put("totalWeight", gdn.getTotalWeight());
                        gdnMap.put("totalPackages", gdn.getTotalPackages());
                        gdnMap.put("pdfUrl", gdn.getPdfUrl());
                        gdnMap.put("hasPdf", gdn.getPdfUrl() != null && !gdn.getPdfUrl().isEmpty());
                        return gdnMap;
                    })
                    .toList();

            log.info("Retrieved {} GDNs successfully", response.size());
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve all GDNs: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve GDNs", e);
        }
    }

    public CartDto convertToCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setDistributorId(cart.getDistributorId());
        dto.setDistributorName(cart.getDistributorName());
        dto.setAddress(cart.getAddress());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList());
        dto.setCartItems(itemDtos);

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
                log.debug("Could not fetch deliveryBy from order tracking for cart {}: {}", cart.getId(), e.getMessage());
            }
        }
        dto.setDeliveryBy(deliveryBy);

        return dto;
    }

    public CartItemDto convertToCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setItemSku(item.getItem().getSku());
        dto.setItemName(item.getItem().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPriceAtTime());
        return dto;
    }

    /**
     * Revert money to distributor ledger when GDN is rejected
     */
    @Transactional
    public void revertMoneyToDistributorLedger(Long orderId, String rejectionReason) {
        log.info("Reverting money to distributor ledger for rejected order: {}", orderId);
        
        try {
            // Get cart to find distributor and order amount
            Cart cart = cartRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Cart not found for order: " + orderId));
            
            if (cart.getDistributorId() == null) {
                log.warn("No distributor ID found for cart: {}", orderId);
                return;
            }
            
            // Calculate total order amount
            BigDecimal orderAmount = cart.getCartItems().stream()
                    .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Order amount is zero or negative for order: {}, skipping reversal", orderId);
                return;
            }
            
            // Create credit transaction to revert the money
            DistributorLedger reversalTransaction = new DistributorLedger();
            reversalTransaction.setDistributorId(cart.getDistributorId());
            reversalTransaction.setAmount(orderAmount);
            reversalTransaction.setTransactionType("CREDIT");
            reversalTransaction.setDescription(String.format("GDN Rejection - Order #%s - %s", orderId, 
                    rejectionReason != null ? rejectionReason : "No reason provided"));
            
            distributorLedgerRepository.save(reversalTransaction);
            
            log.info("Successfully reverted {} to distributor {} ledger for rejected order: {}", 
                    orderAmount, cart.getDistributorId(), orderId);
            
        } catch (Exception e) {
            log.error("Failed to revert money to distributor ledger for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to revert money: " + e.getMessage(), e);
        }
    }

    /**
     * Get weight per unit from FinishedProduct, with fallback to extracting from name.
     * Returns 1.0 as default if no weight can be determined.
     */
    private BigDecimal getWeightPerUnit(com.nector.userservice.model.FinishedProduct product) {
        return getWeightPerUnit(product, product != null ? product.getName() : null);
    }

    /**
     * Get weight per unit from FinishedProduct, with fallback to extracting from product name.
     * Returns 1.0 as default if no weight can be determined.
     */
    private BigDecimal getWeightPerUnit(com.nector.userservice.model.FinishedProduct product, String productName) {
        // First try to get weight from product entity
        if (product != null && product.getWeight() != null && product.getWeight().compareTo(BigDecimal.ZERO) > 0) {
            return product.getWeight();
        }

        // Fallback: try to extract weight from product name (e.g., "MANKA Mash 20 Kg" -> 20)
        if (productName != null && !productName.isEmpty()) {
            BigDecimal extractedWeight = extractWeightFromName(productName);
            if (extractedWeight != null) {
                return extractedWeight;
            }
        }

        // Default fallback
        log.warn("Could not determine weight for product: {}, using default 1.0", productName);
        return BigDecimal.ONE;
    }

    /**
     * Extract weight value from product name.
     * Looks for patterns like "20 Kg", "50 Kg" in the name.
     */
    private BigDecimal extractWeightFromName(String productName) {
        if (productName == null || productName.isEmpty()) {
            return null;
        }
        try {
            String[] words = productName.split(" ");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].matches("\\d+(\\.\\d+)?") &&
                    (words[i + 1].equalsIgnoreCase("Kg") || words[i + 1].equalsIgnoreCase("KG"))) {
                    return new BigDecimal(words[i]);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract weight from product name: {}", productName);
        }
        return null;
    }

}
