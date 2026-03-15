package com.nector.userservice.dispatch.service;

import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.dispatch.dto.GdnItemResponse;
import com.nector.userservice.dispatch.dto.GdnResponse;
import com.nector.userservice.dispatch.dto.GdnGenerationRequest;
import com.nector.userservice.dispatch.dto.InventoryVerificationResponse;
import com.nector.userservice.dispatch.dto.SimpleGdnRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    private final GdnRepository gdnRepository;
    private final CartRepository cartRepository;
    private final InventoryVerificationRepository inventoryVerificationRepository;
    private final InventoryService inventoryService;
    private final HtmlToPdfService htmlToPdfService;
    private final TemplateEngine templateEngine;
    private final CloudinaryStorageService cloudinaryStorageService;
    
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
        gdn.setDispatchFromAddress(request.getDispatchFromAddress());
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

        for (GdnGenerationRequest.InventoryVerificationItem verifiedItem : request.getVerifiedItems()) {
            inventoryService.updateStock(verifiedItem.getItemId(), -verifiedItem.getDispatchQuantity());
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
        
        Gdn gdn = new Gdn();
        gdn.setGdnNumber(generateGdnNumber());
        gdn.setOrderId(orderId);
        gdn.setDispatchFromAddress(request.getDispatchFromAddress());
        gdn.setShippingAddress(request.getShippingAddress());
        gdn.setVehicleNo(request.getVehicleNo());
        gdn.setTransportName(request.getTransportName());
        gdn.setDriverName(request.getDriverName());
        gdn.setDriverMobile(request.getDriverMobile());

        // Create GDN items directly from live cart data
        List<GdnItem> gdnItems = cart.getCartItems().stream()
            .map(cartItem -> {
                GdnItem gdnItem = new GdnItem();
                gdnItem.setGdn(gdn);
                gdnItem.setItemId(cartItem.getItem().getId());
                gdnItem.setItemDescription(cartItem.getItem().getName());
                gdnItem.setNoOfUnitsDispatch(cartItem.getQuantity());
                gdnItem.setWeightPerUnit(BigDecimal.valueOf(1.0)); // Default weight per unit
                return gdnItem;
            })
            .collect(Collectors.toList());

        gdn.setGdnItems(gdnItems);
        gdn.setTotalPackages(gdnItems.stream().mapToInt(GdnItem::getNoOfUnitsDispatch).sum());
        gdn.setTotalWeight(gdnItems.stream()
            .map(item -> item.getWeightPerUnit().multiply(BigDecimal.valueOf(item.getNoOfUnitsDispatch())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Update stock based on live cart quantities
        for (CartItem cartItem : cart.getCartItems()) {
            inventoryService.updateStock(cartItem.getItem().getId(), -cartItem.getQuantity());
        }

        Gdn savedGdn = gdnRepository.save(gdn);
        
        // Generate PDF like proforma invoice
        generateGdnPdf(savedGdn, cart);
        
        return mapToResponse(savedGdn);
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
        
        boolean canProceed = items.stream().allMatch(item -> item.getAvailableQuantity() > 0);
        response.setCanProceedWithGdn(canProceed);
        response.setMessage(canProceed ? 
            "Inventory verified. Proceed with GDN generation." : 
            "Some items have insufficient stock. Adjust quantities before GDN generation.");
        
        saveInventoryVerification(orderId, cart.getId(), items, canProceed);
        
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
        int year = LocalDateTime.now().getYear();
        long count = gdnRepository.count() + 1;
        return String.format("GDN/%d/%04d", year, count);
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
        gdnItem.setWeightPerUnit(BigDecimal.valueOf(1.0)); // Default weight per unit
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
        
        // Get current stock from inventory service
        Integer availableStock = inventoryService.getAvailableStock(itemId);
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
        
        // Transport details
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
        data.put("companyName", "Your Company Name");
        data.put("companyAddress", "Your Company Address");
        data.put("contactNumber", "+91-XXXXXXXXXX");
        
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
    private void generateGdnPdf(Gdn gdn, Cart cart) {
        log.info("=== GDN PDF GENERATION STARTED ===");
        log.info("GDN details - Number: {}, Order ID: {}, Timestamp: {}",
                gdn.getGdnNumber(), gdn.getOrderId(), java.time.LocalDateTime.now());

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
            byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
            log.info("PDF generated successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            // Step 4: Upload to Cloudinary
            log.info("Step 4/4: Uploading PDF to Cloudinary");
            String cloudinaryUrl = uploadGdnPdfToCloudinary(pdfBytes, gdn.getGdnNumber());

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
            // Don't throw exception - GDN is already saved, PDF generation is optional
        }
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
}