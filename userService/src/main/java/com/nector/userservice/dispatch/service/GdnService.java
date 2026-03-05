package com.nector.userservice.dispatch.service;

import com.nector.userservice.dispatch.dto.GdnItemResponse;
import com.nector.userservice.dispatch.dto.GdnResponse;
import com.nector.userservice.dispatch.dto.GdnGenerationRequest;
import com.nector.userservice.dispatch.dto.InventoryVerificationResponse;
import com.nector.userservice.dispatch.entity.Gdn;
import com.nector.userservice.dispatch.entity.GdnItem;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GdnService {
    
    private final GdnRepository gdnRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
    
    @Transactional
    public GdnResponse generateGdn(Long orderId, GdnGenerationRequest request) {
        if (gdnRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("GDN already exists for order: " + orderId);
        }
        
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
        if (cart.getStatus() != Cart.CartStatus.PAYMENT_APPROVED) {
            throw new RuntimeException("Order must be PAYMENT_APPROVED to generate GDN. Current status: " + cart.getStatus());
        }
        
        // Verify inventory and create GDN with verified quantities
        Gdn gdn = new Gdn();
        gdn.setGdnNumber(generateGdnNumber());
        gdn.setOrderId(orderId);
        gdn.setDispatchFromAddress(request.getDispatchFromAddress());
        gdn.setShippingAddress(request.getShippingAddress());
        gdn.setVehicleNo(request.getVehicleNo());
        gdn.setTransportName(request.getTransportName());
        gdn.setDriverName(request.getDriverName());
        gdn.setDriverMobile(request.getDriverMobile());
        
        List<GdnItem> gdnItems = request.getVerifiedItems().stream()
            .map(verifiedItem -> createGdnItemFromVerification(gdn, verifiedItem, cart))
            .collect(Collectors.toList());
        
        gdn.setGdnItems(gdnItems);
        gdn.setTotalPackages(gdnItems.stream().mapToInt(GdnItem::getNoOfUnitsDispatch).sum());
        gdn.setTotalWeight(gdnItems.stream()
            .map(item -> item.getWeightPerUnit().multiply(BigDecimal.valueOf(item.getNoOfUnitsDispatch())))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // Update inventory with actual dispatched quantities
        for (GdnGenerationRequest.InventoryVerificationItem verifiedItem : request.getVerifiedItems()) {
            inventoryService.updateStock(verifiedItem.getItemId(), -verifiedItem.getDispatchQuantity());
        }
        
        Gdn savedGdn = gdnRepository.save(gdn);
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
        
        return response;
    }
    
    public GdnResponse getGdnByOrderId(Long orderId) {
        Gdn gdn = gdnRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("GDN not found for order: " + orderId));
        return mapToResponse(gdn);
    }
    
    private String generateGdnNumber() {
        int year = LocalDateTime.now().getYear();
        long count = gdnRepository.count() + 1;
        return String.format("GDN/%d/%04d", year, count);
    }
    
    private GdnItem createGdnItemFromVerification(Gdn gdn, GdnGenerationRequest.InventoryVerificationItem verifiedItem, Cart cart) {
        CartItem cartItem = cart.getCartItems().stream()
            .filter(item -> item.getItem().getId().equals(verifiedItem.getItemId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Item not found in cart: " + verifiedItem.getItemId()));
            
        GdnItem gdnItem = new GdnItem();
        gdnItem.setGdn(gdn);
        gdnItem.setItemId(cartItem.getItem().getId());
        gdnItem.setItemDescription(cartItem.getItem().getName());
        gdnItem.setNoOfUnitsDispatch(verifiedItem.getDispatchQuantity());
        gdnItem.setWeightPerUnit(BigDecimal.valueOf(1.0)); // Default weight per unit
        return gdnItem;
    }
    
    private InventoryVerificationResponse.InventoryItem verifyItemInventory(CartItem cartItem) {
        InventoryVerificationResponse.InventoryItem item = new InventoryVerificationResponse.InventoryItem();
        item.setItemId(cartItem.getItem().getId());
        item.setItemName(cartItem.getItem().getName());
        item.setItemSku(cartItem.getItem().getSku());
        item.setOrderedQuantity(cartItem.getQuantity());
        
        // Get current stock from inventory service
        Integer availableStock = inventoryService.getAvailableStock(cartItem.getItem().getId());
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
}