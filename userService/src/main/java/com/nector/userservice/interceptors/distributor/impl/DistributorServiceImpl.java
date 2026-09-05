package com.nector.userservice.interceptors.distributor.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;
import com.nector.userservice.interceptors.distributor.dto.DistributorStockResponse;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.distributor.model.*;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.distributor.repository.OrderConfirmationRepository;
import com.nector.userservice.interceptors.distributor.service.DistributorMapper;
import com.nector.userservice.interceptors.distributor.service.DistributorService;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.ledger.dto.CreateLedgerAccountRequest;
import com.nector.userservice.ledger.service.LedgerAccountService;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.DistributorLedgerRepository;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.ItemRepository;
import com.nector.userservice.service.InvoiceService;
// import com.nector.userservice.service.JwtService; // JWT disabled
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import com.nector.userservice.ordertracking.dto.UpdateStepRequest;
import com.nector.userservice.service.DistributorStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nector.userservice.model.User;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.repository.SalesPersonRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DistributorServiceImpl implements DistributorService {
    
    private final DistributorRepository distributorRepository;
    private final DistributorMapper distributorMapper;
    private final LedgerAccountService ledgerAccountService;
    private final OrderConfirmationRepository orderConfirmationRepository;
    private final ItemRepository itemRepository;
    private final FinishedProductRepository finishedProductRepository;
    // private final JwtService jwtService; // JWT disabled
    private final UserRepository userRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final CartRepository cartRepository;
    private final DealerLedgerTransactionRepository dealerLedgerTransactionRepository;
    private final DistributorLedgerRepository distributorLedgerRepository;
    private final InvoiceService invoiceService;
    private final OrderTrackingService orderTrackingService;
    private final DistributorStockService distributorStockService;
    private final com.nector.userservice.service.EmployeeKpiAssignmentService employeeKpiAssignmentService;
    
    @Override
    public DistributorResponseDTO createDistributor(DistributorRequestDTO request) {
        log.info("Creating distributor with email: {}", request.getContactEmail());

        if (distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException(
                    "Distributor with email already exists: " + request.getContactEmail());
        }
        if (distributorRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            throw new IllegalArgumentException(
                    "Distributor with AadhaarNumber already exists: " + request.getAadhaarNumber());
        }
        if (distributorRepository.existsByPanNumber(request.getPanNumber())) {
            throw new IllegalArgumentException(
                    "Distributor with PanNumber already exists: " + request.getPanNumber());
        }

        // Validate salesperson assignment
        if (request.getSalespersonId() != null) {
            if (!salesPersonRepository.existsById(request.getSalespersonId())) {
                throw new IllegalArgumentException(
                        "Sales person not found with ID: " + request.getSalespersonId());
            }
            
            Optional<SalesPerson> salesPerson = salesPersonRepository.findById(request.getSalespersonId());
            if (salesPerson.isPresent() && !salesPerson.get().getActive()) {
                throw new IllegalArgumentException(
                        "Sales person is not active: " + request.getSalespersonId());
            }
        }

        Distributor distributor = distributorMapper.toEntity(request);
        
        // Generate auto distributor code
        String distributorCode = generateDistributorCode();
        distributor.setDistributorCode(distributorCode);
        
        Distributor savedDistributor = distributorRepository.save(distributor);

        // Auto-create ledger account for the distributor
        try {
            CreateLedgerAccountRequest ledgerRequest = new CreateLedgerAccountRequest();
            ledgerRequest.setCompanyId(1L); // Default company ID
            ledgerRequest.setDistributorId(savedDistributor.getId());
            ledgerRequest.setAccountName(savedDistributor.getFirstName() + " - Ledger Account");
            ledgerRequest.setCreditLimit(savedDistributor.getCreditLimit() != null
                    ? savedDistributor.getCreditLimit() : BigDecimal.ZERO);

            ledgerAccountService.createLedgerAccount(ledgerRequest, "system");
            log.info("Ledger account created for distributor: {}", savedDistributor.getId());
        } catch (Exception e) {
            log.warn("Failed to create ledger account for distributor: {}", savedDistributor.getId(), e);
        }

        // Add credit facility as CREDIT entry so the cash ledger reflects the granted credit
        if (savedDistributor.getCreditLimit() != null
                && savedDistributor.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            try {
                com.nector.userservice.model.DistributorLedger creditEntry = new com.nector.userservice.model.DistributorLedger();
                creditEntry.setDistributorId(savedDistributor.getId());
                creditEntry.setAmount(savedDistributor.getCreditLimit());
                creditEntry.setTransactionType("CREDIT");
                creditEntry.setDescription("Credit Facility Granted - Limit Rs." + savedDistributor.getCreditLimit());
                creditEntry.setCreatedAt(LocalDateTime.now());
                distributorLedgerRepository.save(creditEntry);
                log.info("Credit facility CREDIT entry added for distributor {} amount {}",
                        savedDistributor.getId(), savedDistributor.getCreditLimit());
            } catch (Exception e) {
                log.warn("Failed to add credit facility ledger entry for distributor {}: {}",
                        savedDistributor.getId(), e.getMessage());
            }
        }

        log.info("Distributor created successfully with ID: {}", savedDistributor.getId());

        // Auto-update Distributor Onboard KPI for the salesperson
        try {
            if (request.getSalespersonId() != null) {
                employeeKpiAssignmentService.autoUpdateKpiAchieved(
                        request.getSalespersonId(),
                        "Distributor Onboard",
                        java.math.BigDecimal.ONE
                );
                log.info("Auto-updated 'Distributor Onboard' KPI for salesperson {}", request.getSalespersonId());
            }
        } catch (Exception e) {
            log.warn("Failed to auto-update 'Distributor Onboard' KPI for salesperson {}: {}",
                    request.getSalespersonId(), e.getMessage());
        }

        return distributorMapper.toResponseDTO(savedDistributor);
    }

    @Override
    @Transactional(readOnly = true)
    public DistributorResponseDTO getDistributorById(Long id) {
        log.info("Fetching distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        return distributorMapper.toResponseDTO(distributor);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DistributorResponseDTO> getAllDistributors() {
        log.info("Fetching all distributors");
        
        return distributorRepository.findAll()
                .stream()
                .map(distributorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public DistributorResponseDTO updateDistributor(Long id, DistributorRequestDTO request) {
        log.info("Updating distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!distributor.getContactEmail().equals(request.getContactEmail()) && 
            distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Distributor with email already exists: " + request.getContactEmail());
        }
        
        distributorMapper.updateEntity(distributor, request);
        Distributor updatedDistributor = distributorRepository.save(distributor);
        
        log.info("Distributor updated successfully with ID: {}", updatedDistributor.getId());
        return distributorMapper.toResponseDTO(updatedDistributor);
    }
    
    @Override
    public void deleteDistributor(Long id) {
        log.info("Deleting distributor with ID: {}", id);
        
        if (!distributorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Distributor not found with ID: " + id);
        }

        dealerLedgerTransactionRepository.deleteByDistributorId(id);
        distributorLedgerRepository.deleteByDistributorId(id);
        
        distributorRepository.deleteById(id);
        log.info("Distributor deleted successfully with ID: {}", id);
    }
    
    @Override
    public OrderConfirmationResponse confirmOrderReceived(Long distributorId, OrderConfirmationRequest request) {
        log.info("Processing order confirmation for distributor: {} and order: {}", distributorId, request.getOrderId());

        if (orderConfirmationRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalArgumentException("Order already confirmed: " + request.getOrderId());
        }

        boolean requiresApproval = hasDamagedOrPartialItemCondition(request);

        OrderConfirmation confirmation = new OrderConfirmation();
        confirmation.setOrderId(request.getOrderId());
        confirmation.setDistributorId(distributorId);
        confirmation.setGdnNumber(request.getGdnNumber());
        confirmation.setStatus(request.getStatus());
        confirmation.setOverallRating(request.getOverallRating());
        confirmation.setFeedback(request.getFeedback());
        confirmation.setRemarks(request.getRemarks());
        confirmation.setApprovalStatus(requiresApproval ?
                OrderConfirmation.ApprovalStatus.PENDING_APPROVAL :
                OrderConfirmation.ApprovalStatus.NOT_REQUIRED);

        if (request.getItemConfirmations() != null) {
            List<ItemConfirmationEntity> itemConfirmations = request.getItemConfirmations().stream()
                .map(item -> {
                    ItemConfirmationEntity entity = new ItemConfirmationEntity();
                    entity.setOrderConfirmation(confirmation);
                    entity.setItemId(item.getItemId());

                    String sku = item.getSku();
                    if (sku == null || sku.trim().isEmpty()) {
                        sku = finishedProductRepository.findById(item.getItemId())
                                .map(product -> product.getSku())
                                .orElse(null);
                        if (sku == null) {
                            log.warn("SKU not found for itemId: {}. Order confirmation may have incomplete data.", item.getItemId());
                        } else {
                            log.info("Auto-populated SKU '{}' for itemId: {}", sku, item.getItemId());
                        }
                    }
                    entity.setSku(sku);

                    entity.setDispatchedQuantity(item.getDispatchedQuantity());
                    entity.setReceivedQuantity(item.getReceivedQuantity());
                    entity.setCondition(item.getCondition());
                    entity.setItemRemarks(item.getItemRemarks());
                    return entity;
                })
                .collect(Collectors.toList());
            confirmation.setItemConfirmations(itemConfirmations);
        }

        OrderConfirmation savedConfirmation = orderConfirmationRepository.save(confirmation);
        log.info("Order confirmation saved with ID: {}", savedConfirmation.getId());

        if (requiresApproval) {
            log.info("Order {} has DAMAGED/PARTIAL items. Pending admin approval — no stock, tracking, or invoice updates.", request.getOrderId());
            return mapToResponse(savedConfirmation);
        }

        // Add received stock to distributor inventory (+ stock operation)
        if (savedConfirmation.getItemConfirmations() != null) {
            for (ItemConfirmationEntity itemConfirmation : savedConfirmation.getItemConfirmations()) {
                if (itemConfirmation.getReceivedQuantity() != null && itemConfirmation.getReceivedQuantity() > 0) {
                    try {
                        distributorStockService.addStock(
                            distributorId,
                            itemConfirmation.getItemId(),
                            itemConfirmation.getSku(),
                            itemConfirmation.getReceivedQuantity()
                        );
                        log.info("Added {} units of SKU: {} to distributor {} stock",
                            itemConfirmation.getReceivedQuantity(),
                            itemConfirmation.getSku(),
                            distributorId);
                    } catch (Exception e) {
                        log.error("Failed to add stock for itemId: {} - {}", itemConfirmation.getItemId(), e.getMessage());
                    }
                }
            }
        }

        // Check if order has short or damaged remarks
        boolean hasShortOrDamaged = hasShortOrDamagedRemarks(request);
        if (hasShortOrDamaged) {
            log.info("Order {} has short/damaged remarks. Invoice will not be generated.", request.getOrderId());
        }

        // Update Order Tracking Step 11: Order Received
        try {
            com.nector.userservice.ordertracking.entity.OrderTracking order =
                orderTrackingService.getOrderRepository().findByCartId(request.getOrderId());

            if (order != null) {
                boolean isReceived = request.getStatus() == OrderConfirmationRequest.ConfirmationStatus.RECEIVED_COMPLETE ||
                                  request.getStatus() == OrderConfirmationRequest.ConfirmationStatus.RECEIVED_PARTIAL;

                UpdateStepRequest step11Request = new UpdateStepRequest();

                if (hasShortOrDamaged) {
                    step11Request.setStatus("in-progress");
                } else {
                    step11Request.setStatus(isReceived ? "completed" : "cancelled");
                }

                step11Request.setRemarks(isReceived ?
                    "Order received by distributor: " + request.getFeedback() :
                    "Order not received: " + request.getRemarks());
                step11Request.setDate(java.time.LocalDate.now().toString());

                step11Request.setAssignedPersonId(distributorId);
                step11Request.setHasAction(true);
                step11Request.setActionResponse(isReceived ? "yes" : "no");

                distributorRepository.findById(distributorId).ifPresent(distributor -> {
                    step11Request.setAssignedPersonName(distributor.getFirstName() + " " + distributor.getLastName());
                    step11Request.setAssignedPersonRole("DISTRIBUTOR");
                    step11Request.setAssignedPersonEmail(distributor.getContactEmail());
                    step11Request.setAssignedPersonPhone(distributor.getPhoneNumber());
                });

                orderTrackingService.updateStepBySequence(order.getId(), 11, step11Request);
                log.info("Order tracking Step 11 updated for order {} - received: {}", request.getOrderId(), isReceived);
            }
        } catch (Exception e) {
            log.error("Failed to update Order Tracking Step 11 for order {}: {}", request.getOrderId(), e.getMessage());
        }

        // Generate and save invoice after order confirmation (only if no short/damaged remarks)
        if (!hasShortOrDamaged) {
            try {
                String invoiceResult = invoiceService.generateInvoice(savedConfirmation.getId());
                log.info("Invoice generated successfully for order confirmation ID: {}", savedConfirmation.getId());
            } catch (Exception e) {
                log.error("Failed to generate invoice for order confirmation ID: {} - {}", savedConfirmation.getId(), e.getMessage());
            }
        }

        return mapToResponse(savedConfirmation);
    }

    @Override
    public List<OrderConfirmationResponse> getPendingApprovalConfirmations() {
        return orderConfirmationRepository
                .findByApprovalStatusOrderByConfirmedAtDesc(OrderConfirmation.ApprovalStatus.PENDING_APPROVAL)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public OrderConfirmationResponse processOrderConfirmationApproval(Long confirmationId, String action, String adminComment) {
        OrderConfirmation confirmation = orderConfirmationRepository.findByIdWithItems(confirmationId)
                .orElseThrow(() -> new com.nector.userservice.exception.ResourceNotFoundException(
                        "Order confirmation not found: " + confirmationId));

        if (confirmation.getApprovalStatus() != OrderConfirmation.ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException(
                    "Order confirmation " + confirmationId + " is not pending approval (current status: " + confirmation.getApprovalStatus() + ")");
        }

        confirmation.setAdminComment(adminComment);

        if ("APPROVE".equalsIgnoreCase(action)) {
            // Deferred stock update
            if (confirmation.getItemConfirmations() != null) {
                for (ItemConfirmationEntity item : confirmation.getItemConfirmations()) {
                    if (item.getReceivedQuantity() != null && item.getReceivedQuantity() > 0) {
                        try {
                            distributorStockService.addStock(
                                    confirmation.getDistributorId(),
                                    item.getItemId(),
                                    item.getSku(),
                                    item.getReceivedQuantity()
                            );
                            log.info("Post-approval: added {} units of SKU: {} to distributor {} stock",
                                    item.getReceivedQuantity(), item.getSku(), confirmation.getDistributorId());
                        } catch (Exception e) {
                            log.error("Post-approval: failed to add stock for itemId: {} - {}", item.getItemId(), e.getMessage());
                        }
                    }
                }
            }

            // Deferred order tracking Step 11
            try {
                com.nector.userservice.ordertracking.entity.OrderTracking order =
                        orderTrackingService.getOrderRepository().findByCartId(confirmation.getOrderId());

                if (order != null) {
                    boolean isReceived = confirmation.getStatus() == OrderConfirmationRequest.ConfirmationStatus.RECEIVED_COMPLETE ||
                                        confirmation.getStatus() == OrderConfirmationRequest.ConfirmationStatus.RECEIVED_PARTIAL;

                    UpdateStepRequest step11Request = new UpdateStepRequest();
                    step11Request.setStatus(isReceived ? "completed" : "cancelled");
                    step11Request.setRemarks("Admin approved order with DAMAGED/PARTIAL items. Comment: " +
                            (adminComment != null ? adminComment : "None"));
                    step11Request.setDate(java.time.LocalDate.now().toString());
                    step11Request.setAssignedPersonId(confirmation.getDistributorId());
                    step11Request.setHasAction(true);
                    step11Request.setActionResponse(isReceived ? "yes" : "no");

                    distributorRepository.findById(confirmation.getDistributorId()).ifPresent(distributor -> {
                        step11Request.setAssignedPersonName(distributor.getFirstName() + " " + distributor.getLastName());
                        step11Request.setAssignedPersonRole("DISTRIBUTOR");
                        step11Request.setAssignedPersonEmail(distributor.getContactEmail());
                        step11Request.setAssignedPersonPhone(distributor.getPhoneNumber());
                    });

                    orderTrackingService.updateStepBySequence(order.getId(), 11, step11Request);
                    log.info("Post-approval: order tracking Step 11 updated for order {}", confirmation.getOrderId());
                }
            } catch (Exception e) {
                log.error("Post-approval: failed to update Order Tracking Step 11 for order {}: {}",
                        confirmation.getOrderId(), e.getMessage());
            }

            // Deferred invoice generation
            try {
                invoiceService.generateInvoice(confirmation.getId());
                log.info("Post-approval: invoice generated for order confirmation ID: {}", confirmation.getId());
            } catch (Exception e) {
                log.error("Post-approval: failed to generate invoice for order confirmation ID: {} - {}",
                        confirmation.getId(), e.getMessage());
            }

            confirmation.setApprovalStatus(OrderConfirmation.ApprovalStatus.APPROVED);
            log.info("Order confirmation {} approved by admin", confirmationId);

        } else if ("REJECT".equalsIgnoreCase(action)) {
            // Update tracking to reflect rejection
            try {
                com.nector.userservice.ordertracking.entity.OrderTracking order =
                        orderTrackingService.getOrderRepository().findByCartId(confirmation.getOrderId());

                if (order != null) {
                    UpdateStepRequest step11Request = new UpdateStepRequest();
                    step11Request.setStatus("cancelled");
                    step11Request.setRemarks("Order rejected by admin due to DAMAGED/PARTIAL items. Comment: " +
                            (adminComment != null ? adminComment : "None"));
                    step11Request.setDate(java.time.LocalDate.now().toString());
                    step11Request.setAssignedPersonId(confirmation.getDistributorId());
                    step11Request.setHasAction(true);
                    step11Request.setActionResponse("no");

                    distributorRepository.findById(confirmation.getDistributorId()).ifPresent(distributor -> {
                        step11Request.setAssignedPersonName(distributor.getFirstName() + " " + distributor.getLastName());
                        step11Request.setAssignedPersonRole("DISTRIBUTOR");
                        step11Request.setAssignedPersonEmail(distributor.getContactEmail());
                        step11Request.setAssignedPersonPhone(distributor.getPhoneNumber());
                    });

                    orderTrackingService.updateStepBySequence(order.getId(), 11, step11Request);
                    log.info("Post-rejection: order tracking Step 11 set to cancelled for order {}", confirmation.getOrderId());
                }
            } catch (Exception e) {
                log.error("Post-rejection: failed to update Order Tracking Step 11 for order {}: {}",
                        confirmation.getOrderId(), e.getMessage());
            }

            confirmation.setApprovalStatus(OrderConfirmation.ApprovalStatus.REJECTED);
            log.info("Order confirmation {} rejected by admin", confirmationId);

        } else {
            throw new IllegalArgumentException("Invalid action: " + action + ". Must be APPROVE or REJECT.");
        }

        return mapToResponse(orderConfirmationRepository.save(confirmation));
    }

    private boolean hasDamagedOrPartialItemCondition(OrderConfirmationRequest request) {
        if (request.getItemConfirmations() == null) return false;
        return request.getItemConfirmations().stream()
                .anyMatch(item -> item.getCondition() == OrderConfirmationRequest.ItemCondition.DAMAGED ||
                                  item.getCondition() == OrderConfirmationRequest.ItemCondition.PARTIAL);
    }

    /**
     * Check if the order confirmation request has short or damaged remarks
     */
    private boolean hasShortOrDamagedRemarks(OrderConfirmationRequest request) {
        // Check main remarks
        if (request.getRemarks() != null) {
            String remarks = request.getRemarks().toLowerCase();
            if (remarks.contains("short") || remarks.contains("damaged")) {
                return true;
            }
        }
        
        // Check confirmation status
        if (request.getStatus() == OrderConfirmationRequest.ConfirmationStatus.DAMAGED) {
            return true;
        }
        
        // Check item-level remarks and conditions
        if (request.getItemConfirmations() != null) {
            for (OrderConfirmationRequest.ItemConfirmation item : request.getItemConfirmations()) {
                // Check item remarks
                if (item.getItemRemarks() != null) {
                    String itemRemarks = item.getItemRemarks().toLowerCase();
                    if (itemRemarks.contains("short") || itemRemarks.contains("damaged")) {
                        return true;
                    }
                }
                
                // Check item condition
                if (item.getCondition() == OrderConfirmationRequest.ItemCondition.DAMAGED ||
                    item.getCondition() == OrderConfirmationRequest.ItemCondition.PARTIAL) {
                    return true;
                }
                
                // Check for short quantity (received less than dispatched)
                if (item.getReceivedQuantity() != null && item.getDispatchedQuantity() != null) {
                    if (item.getReceivedQuantity() < item.getDispatchedQuantity()) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public OrderConfirmationResponse getOrderConfirmation(Long orderId) {
        OrderConfirmation confirmation = orderConfirmationRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order confirmation not found for order: " + orderId));
        return mapToResponse(confirmation);
    }
    
    @Override
    public List<OrderConfirmationResponse> getDistributorConfirmations(Long distributorId) {
        List<OrderConfirmation> confirmations = orderConfirmationRepository.findByDistributorIdOrderByConfirmedAtDesc(distributorId);
        return confirmations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private OrderConfirmationResponse mapToResponse(OrderConfirmation confirmation) {
        OrderConfirmationResponse response = new OrderConfirmationResponse();
        response.setId(confirmation.getId());
        response.setOrderId(confirmation.getOrderId());
        response.setDistributorId(confirmation.getDistributorId());
        response.setGdnNumber(confirmation.getGdnNumber());
        response.setStatus(confirmation.getStatus());
        response.setOverallRating(confirmation.getOverallRating());
        response.setFeedback(confirmation.getFeedback());
        response.setRemarks(confirmation.getRemarks());
        response.setApprovalStatus(confirmation.getApprovalStatus());
        response.setAdminComment(confirmation.getAdminComment());
        response.setConfirmedAt(confirmation.getConfirmedAt());
        
        if (confirmation.getItemConfirmations() != null) {
            List<OrderConfirmationResponse.ItemConfirmationResponse> itemResponses = confirmation.getItemConfirmations().stream()
                .map(item -> {
                    OrderConfirmationResponse.ItemConfirmationResponse itemResponse = new OrderConfirmationResponse.ItemConfirmationResponse();
                    itemResponse.setItemId(item.getItemId());
                    itemResponse.setDispatchedQuantity(item.getDispatchedQuantity());
                    itemResponse.setReceivedQuantity(item.getReceivedQuantity());
                    itemResponse.setCondition(item.getCondition());
                    itemResponse.setItemRemarks(item.getItemRemarks());
                    
                    // Get item name from repository
                    itemRepository.findById(item.getItemId())
                        .ifPresent(itemEntity -> itemResponse.setItemName(itemEntity.getName()));
                    
                    return itemResponse;
                })
                .collect(Collectors.toList());
            response.setItemConfirmations(itemResponses);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getAllOrdersForDistributor(Long distributorId) {
        log.info("Fetching all orders for distributor: {}", distributorId);

        // Get all carts and filter by distributor ID
        List<Cart> allCarts = cartRepository.findAll();
        return allCarts.stream()
                .filter(cart -> {
                    try {
                        return cart.getDistributorId() != null && 
                               cart.getDistributorId().equals(distributorId);
                    } catch (Exception e) {
                        log.warn("Invalid distributorId for cart ID: {}, value: {}", cart.getId(), cart.getDistributorId());
                        return false;
                    }
                })
                .map(cart -> {
                    CartResponse response = new CartResponse();
                    response.setId(cart.getId());
                    response.setStatus(cart.getStatus().name());
                    response.setCreatedAt(cart.getCreatedAt());
                    response.setUpdatedAt(cart.getUpdatedAt());
                    response.setDistributorId(cart.getDistributorId());

                    // Set distributor name
                    distributorRepository.findById(cart.getDistributorId()).ifPresent(distributor -> {
                        response.setDistributorName(distributor.getFirstName());
                    });

                    // Map cart items
                    List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                            .map(cartItem -> {
                                CartItemResponse itemResponse = new CartItemResponse();
                                itemResponse.setId(cartItem.getId());
                                itemResponse.setItemId(String.valueOf(cartItem.getItem().getId()));
                                itemResponse.setItemName(cartItem.getItem().getName());
                                itemResponse.setItemSku(cartItem.getItem().getSku());
                                itemResponse.setQuantity(cartItem.getQuantity());
                                itemResponse.setPriceAtTime(cartItem.getPriceAtTime());
                                itemResponse.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                                return itemResponse;
                            })
                            .collect(Collectors.toList());

                    response.setCartItems(cartItemResponses);

                    // Calculate total cart amount
                    BigDecimal totalAmount = cartItemResponses.stream()
                            .map(CartItemResponse::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    response.setTotalCartAmount(totalAmount);

                    return response;
                })
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDistributorAddress(Long distributorId) {
        log.info("Fetching address for distributor with ID: {}", distributorId);
        
        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + distributorId));
        
        AddressResponse response = new AddressResponse();
        response.setDistributorId(distributor.getId());
        response.setAddress(distributor.getAddress());
        
        return response;
    }
    
    /**
     * Generates distributor code in format: DIS+MM+YYYY+SR.NO
     * Where SR.NO is a 3-digit sequence number for the current month
     */
    private String generateDistributorCode() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        
        // Get count of distributors created in current month and year
        long count = distributorRepository.countByMonthAndYear(month, year);
        
        // Generate 3-digit sequence number (count + 1)
        String sequence = String.format("%03d", count + 1);
        
        // Format: DIS+MM+YYYY+SR.NO
        return String.format("DIS%02d%d%s", month, year, sequence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistributorResponseDTO> getDistributorsBySalespersonId(Long salespersonId) {
        log.info("Fetching distributors for salesperson ID: {}", salespersonId);

        List<Distributor> distributors = distributorRepository.findBySalespersonId(salespersonId);
        return distributors.stream()
                .map(distributorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DistributorStockResponse getDistributorStock(Long distributorId) {
        log.info("Fetching stock for distributor: {}", distributorId);

        // Validate distributor exists
        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + distributorId));

        // Get all current stock from DistributorStock entity (already accounts for +received and -sold)
        List<com.nector.userservice.model.DistributorStock> stocks = 
                distributorStockService.getStocksByDistributorId(distributorId);

        // Map to response StockItem
        List<DistributorStockResponse.StockItem> stockItems = stocks.stream()
            .filter(stock -> stock.getQuantity() > 0) // Only show items with positive quantity
            .map(stock -> {
                DistributorStockResponse.StockItem item = new DistributorStockResponse.StockItem();
                item.setItemId(stock.getItemId());
                item.setItemSku(stock.getSku());
                item.setItemName(stock.getItemName());
                item.setTotalQuantity(stock.getQuantity());
                item.setUnitType(stock.getUnitType());
                
                // Calculate total value using current price
                finishedProductRepository.findById(stock.getItemId()).ifPresent(product -> {
                    item.setUnitPrice(product.getPrice());
                    if (product.getPrice() != null) {
                        item.setTotalValue(product.getPrice().multiply(BigDecimal.valueOf(stock.getQuantity())));
                    }
                });
                
                return item;
            })
            .collect(Collectors.toList());

        // Build response
        DistributorStockResponse response = new DistributorStockResponse();
        response.setDistributorId(distributorId);
        response.setDistributorName(distributor.getFirstName() + " " + distributor.getLastName());
        response.setTotalUniqueItems(stockItems.size());
        response.setStockItems(stockItems);

        // Calculate totals
        int totalQuantity = stockItems.stream()
                .mapToInt(DistributorStockResponse.StockItem::getTotalQuantity)
                .sum();
        response.setTotalQuantity(totalQuantity);

        BigDecimal totalStockValue = stockItems.stream()
                .map(DistributorStockResponse.StockItem::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalStockValue(totalStockValue);
        response.setLastUpdated(LocalDateTime.now());

        log.info("Stock fetched for distributor {}: {} unique items, {} total quantity (after dealer billing deductions)",
                distributorId, response.getTotalUniqueItems(), response.getTotalQuantity());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDispatchReport(Long distributorId) {
        log.info("Fetching dispatch report for distributor: {}", distributorId);

        List<Cart.CartStatus> excludedForPending = List.of(Cart.CartStatus.GDN_GENERATED, Cart.CartStatus.DISMISSED);

        List<Cart> allCarts = cartRepository.findByDistributorIdAndStatusIn(distributorId,
                Arrays.asList(Cart.CartStatus.values()));

        long pending = allCarts.stream()
                .filter(c -> !excludedForPending.contains(c.getStatus()))
                .count();

        long dispatched = allCarts.stream()
                .filter(c -> c.getStatus() == Cart.CartStatus.GDN_GENERATED)
                .count();

        long delivered = orderConfirmationRepository.findByDistributorIdOrderByConfirmedAtDesc(distributorId).size();

        Map<String, Long> report = new LinkedHashMap<>();
        report.put("pending", pending);
        report.put("dispatched", dispatched);
        report.put("delivered", delivered);

        log.info("Dispatch report for distributor {}: pending={}, dispatched={}, delivered={}",
                distributorId, pending, dispatched, delivered);

        return report;
    }
}
