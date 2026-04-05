package com.nector.userservice.service;

import com.nector.userservice.dto.payment.*;
import com.nector.userservice.interceptors.accounts.model.PaymentHistoryResponse;
import com.nector.userservice.interceptors.accounts.model.PaymentHistoryWithRunningBalanceResponse;
import com.nector.userservice.model.PaymentApproval;
import com.nector.userservice.model.ProformaInvoice;
import com.nector.userservice.model.DistributorLedger;
import com.nector.userservice.model.Cart;
import com.nector.userservice.repository.*;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.interceptors.salesMapping.model.MappingStatus;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import com.nector.userservice.ordertracking.dto.UpdateStepRequest;
import com.nector.userservice.ordertracking.dto.CreateOrderTrackingRequest;
import com.nector.userservice.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;

@Service
public class PaymentService {
    
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    
    @Autowired
    private DistributorLedgerRepository distributorLedgerRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private SalesMappingRepository salesMappingRepository;
    
    @Autowired
    private DistributorRepository distributorRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentApprovalRepository paymentApprovalRepository;

    @Autowired
    private OrderTrackingService orderTrackingService;
    
    @Autowired
    private RbacService rbacService;
    
    /**
     * Get current user ID from security context
     * For now, returns hardcoded user ID 1L (same as RbacService)
     * TODO: Replace with actual authentication context when available
     */
    private Long getCurrentUserId() {
        // Currently using hardcoded user ID as per RbacService implementation
        // When authentication is properly implemented, this should get the actual current user ID
        return 1L;
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
            request.setAssignedPersonPhone(user.getContactNo() != null ? user.getContactNo() : "1800-ACCOUNTS");
        });
    }

    
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        // Process payment and update distributor ledger
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(System.currentTimeMillis()); // Mock ID
        response.setDistributorId(paymentRequest.getDistributorId());
        response.setAmount(paymentRequest.getAmount());
        response.setPaymentMethod(paymentRequest.getPaymentMethod());
        response.setTransactionReference(paymentRequest.getTransactionReference());
        response.setStatus("PROCESSED");
        response.setProcessedAt(LocalDateTime.now());
        
        // Update distributor ledger logic would go here
        updateDistributorLedger(paymentRequest.getDistributorId(), paymentRequest.getAmount());
        
        return response;
    }
    
    public OrderApprovalResponse checkAndApproveOrder(Long orderId, Long distributorId) {
        // Get cart and calculate total amount
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Cart not found: " + orderId));
        
        BigDecimal orderAmount = cart.getCartItems().stream()
            .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal ledgerBalance = getDistributorBalance(distributorId);
        
        OrderApprovalResponse response = new OrderApprovalResponse();
        response.setOrderId(orderId);
        response.setDistributorId(distributorId);
        response.setOrderAmount(orderAmount);
        response.setLedgerBalance(ledgerBalance);
        
        if (ledgerBalance.compareTo(orderAmount) >= 0) {
            response.setStatus("APPROVED");
            response.setMessage("Order approved - sufficient balance");
            
            // Ensure order tracking exists before updating steps
            ensureOrderTrackingExists(orderId);
            
            // Update order tracking Step 4: PI Generated
            updatePIGenerationStep(orderId);
            
            // Update order tracking Step 5: Awaiting Payment Confirmation
            updateAwaitingPaymentStep(orderId);
            
            // Deduct amount from distributor balance
            updateDistributorLedger(distributorId, orderAmount.negate());
            // Update PI payment status
            updateProformaInvoiceStatus(orderId, ProformaInvoice.PaymentStatus.PAID);
            // Update Cart status to PAYMENT_APPROVED
            cart.setStatus(Cart.CartStatus.PAYMENT_APPROVED);
            cartRepository.save(cart);
            
            // Update order tracking Step 6: Approved from Accounts
            updatePaymentApprovedStep(orderId);
        } else {
            response.setStatus("REJECTED");
            response.setMessage("Insufficient balance");
            updateProformaInvoiceStatus(orderId, ProformaInvoice.PaymentStatus.REJECTED);
        }
        
        return response;
    }
    
    public boolean isSalespersonAuthorizedForDistributor(Long salespersonId, Long distributorId) {
        return distributorRepository.findById(distributorId)
            .flatMap(distributor -> userRepository.findById(salespersonId)
                .filter(user -> user.getRoleType().name().equals(distributor.getAssignedPerson())))
            .isPresent();
    }

    public List<PaymentApproval> getPaymentsByDistributor(Long distributorId) {
        return paymentApprovalRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
    }

    public List<PaymentApproval> getPaymentsByDistributorAndStatus(Long distributorId, String status) {
        return paymentApprovalRepository.findByDistributorIdAndStatusOrderByCreatedAtDesc(distributorId, status);
    }
    
    public boolean isCartApproved(Long cartId) {
        return cartRepository.findById(cartId)
            .map(cart -> cart.getStatus() == Cart.CartStatus.APPROVED)
            .orElse(false);
    }
    
    public List<ProformaInvoice> getPendingPIPayments() {
        return proformaInvoiceRepository.findAll().stream()
            .filter(pi -> pi.getPaymentStatus() == ProformaInvoice.PaymentStatus.PENDING)
            .toList();
    }
    
    public void updateDistributorBalance(Long distributorId, BigDecimal amount, String transactionType, String description) {
        DistributorLedger ledger = new DistributorLedger();
        ledger.setDistributorId(distributorId);
        ledger.setAmount(amount);
        ledger.setTransactionType(transactionType);
        ledger.setDescription(description);
        distributorLedgerRepository.save(ledger);
    }
    
    public List<DistributorLedger> getPaymentHistory(Long distributorId) {
        return distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
    }
    
    public PaymentHistoryResponse getPaymentHistoryWithBalance(Long distributorId) {
        List<DistributorLedger> paymentHistory = distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
        BigDecimal closingBalance = distributorLedgerRepository.getDistributorBalance(distributorId);
        
        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setPaymentHistory(paymentHistory);
        response.setClosingBalance(closingBalance);
        response.setDistributorId(distributorId);
        
        return response;
    }
    
    public PaymentHistoryWithRunningBalanceResponse getPaymentHistoryWithRunningBalance(Long distributorId) {
        List<DistributorLedger> paymentHistory = distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
        BigDecimal closingBalance = distributorLedgerRepository.getDistributorBalance(distributorId);
        
        // Calculate running balance for each transaction
        List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> paymentHistoryWithBalance = 
            calculateRunningBalance(paymentHistory, closingBalance);
        
        PaymentHistoryWithRunningBalanceResponse response = new PaymentHistoryWithRunningBalanceResponse();
        response.setPaymentHistory(paymentHistoryWithBalance);
        response.setClosingBalance(closingBalance);
        response.setDistributorId(distributorId);
        
        return response;
    }
    
    private List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> calculateRunningBalance(
            List<DistributorLedger> paymentHistory, BigDecimal closingBalance) {
        
        List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> result = new ArrayList<>();
        
        // Calculate opening balance by working backwards from closing balance
        BigDecimal openingBalance = closingBalance;
        List<DistributorLedger> reversedHistory = new ArrayList<>(paymentHistory);
        Collections.reverse(reversedHistory); // Now oldest to newest
        
        // Calculate opening balance by subtracting all transactions from closing balance
        for (DistributorLedger transaction : reversedHistory) {
            if ("DEBIT".equalsIgnoreCase(transaction.getTransactionType())) {
                openingBalance = openingBalance.add(transaction.getAmount());
            } else if ("CREDIT".equalsIgnoreCase(transaction.getTransactionType())) {
                openingBalance = openingBalance.subtract(transaction.getAmount());
            }
        }
        
        // Now calculate running balance forward from opening balance
        BigDecimal runningBalance = openingBalance;
        
        for (DistributorLedger transaction : reversedHistory) {
            PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance item = 
                new PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance();
            
            // Apply transaction to get new balance
            if ("DEBIT".equalsIgnoreCase(transaction.getTransactionType())) {
                runningBalance = runningBalance.subtract(transaction.getAmount());
            } else if ("CREDIT".equalsIgnoreCase(transaction.getTransactionType())) {
                runningBalance = runningBalance.add(transaction.getAmount());
            }
            
            item.setId(transaction.getId());
            item.setAmount(transaction.getAmount());
            item.setCreatedAt(transaction.getCreatedAt());
            item.setDescription(transaction.getDescription());
            item.setDistributorId(transaction.getDistributorId());
            item.setTransactionType(transaction.getTransactionType());
            item.setCurrentCB(runningBalance); // Balance AFTER this transaction
            
            result.add(item);
        }
        
        // Reverse the list to maintain chronological order (newest first)
        Collections.reverse(result);
        
        return result;
    }
    
    public com.nector.userservice.dto.payment.OrderApprovalResponse approvePaymentForOrder(Long orderId, Long distributorId) {
        // Get PI for the order
        ProformaInvoice pi = proformaInvoiceRepository.findByCartId(orderId)
            .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for order: " + orderId));
        
        // Check if distributor has sufficient balance
        BigDecimal ledgerBalance = getDistributorBalance(distributorId);
        BigDecimal piAmount = pi.getAmount();
        
        com.nector.userservice.dto.payment.OrderApprovalResponse response = new com.nector.userservice.dto.payment.OrderApprovalResponse();
        response.setOrderId(orderId);
        response.setDistributorId(distributorId);
        response.setOrderAmount(piAmount);
        response.setLedgerBalance(ledgerBalance);
        
        if (ledgerBalance.compareTo(piAmount) >= 0) {
            // Ensure order tracking exists before updating steps
            ensureOrderTrackingExists(orderId);
            
            // Update order tracking Step 5: Awaiting Payment Confirmation
            updateAwaitingPaymentStep(orderId);
            
            // Deduct amount from distributor ledger
            updateDistributorBalance(distributorId, piAmount, "DEBIT", "Payment for Order #" + orderId);
            
            // Update PI status to PAID
            pi.setPaymentStatus(ProformaInvoice.PaymentStatus.PAID);
            proformaInvoiceRepository.save(pi);
            
            // Update Cart status to PAYMENT_APPROVED
            Cart cart = cartRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            cart.setStatus(Cart.CartStatus.PAYMENT_APPROVED);
            cartRepository.save(cart);
            
            // Update order tracking Step 6: Approved from Accounts
            updatePaymentApprovedStep(orderId);
            
            response.setStatus("PAYMENT_APPROVED");
            response.setMessage("Payment approved - Order ready for dispatch");
        } else {
            response.setStatus("INSUFFICIENT_BALANCE");
            response.setMessage("Insufficient balance. Required: " + piAmount + ", Available: " + ledgerBalance);
        }
        
        return response;
    }
    
    public List<ProformaInvoice> getPendingPaymentApprovals() {
        return proformaInvoiceRepository.findAll().stream()
            .filter(pi -> pi.getPaymentStatus() == ProformaInvoice.PaymentStatus.PENDING)
            .toList();
    }
    
    private void updateProformaInvoiceStatus(Long cartId, ProformaInvoice.PaymentStatus status) {
        proformaInvoiceRepository.findByCartId(cartId).ifPresent(pi -> {
            pi.setPaymentStatus(status);
            proformaInvoiceRepository.save(pi);
        });
    }
    
    private java.math.BigDecimal getDistributorBalance(Long distributorId) {
        return distributorLedgerRepository.getDistributorBalance(distributorId);
    }
    
    private void updateDistributorLedger(Long distributorId, java.math.BigDecimal amount) {
        String transactionType = amount.compareTo(BigDecimal.ZERO) > 0 ? "CREDIT" : "DEBIT";
        String description = amount.compareTo(BigDecimal.ZERO) > 0 ? "Payment received" : "Order deduction";
        updateDistributorBalance(distributorId, amount.abs(), transactionType, description);
    }

    public Long addPaymentForApproval(Long distributorId, BigDecimal amount, String transactionType, String description) {
        PaymentApproval payment = new PaymentApproval();
        payment.setDistributorId(distributorId);
        payment.setAmount(amount);
        payment.setTransactionType(transactionType);
        payment.setDescription(description);
        payment.setStatus("PAYMENT_ADDED");
        PaymentApproval savedPayment = paymentApprovalRepository.save(payment);
        return savedPayment.getId();
    }

    public Long addPaymentForApprovalWithSalesperson(Long distributorId, Long salespersonId, BigDecimal amount, String transactionType, String description) {
        PaymentApproval payment = new PaymentApproval();
        payment.setDistributorId(distributorId);
        payment.setSalespersonId(salespersonId);
        payment.setAmount(amount);
        payment.setTransactionType(transactionType);
        payment.setDescription(description);
        payment.setStatus("PAYMENT_ADDED");
        PaymentApproval savedPayment = paymentApprovalRepository.save(payment);
        return savedPayment.getId();
    }

    public void approvePayment(Long paymentId, Long approvedBy) {
        PaymentApproval payment = paymentApprovalRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!"PAYMENT_ADDED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment already processed");
        }

        // Update distributor ledger
        updateDistributorBalance(payment.getDistributorId(), payment.getAmount(),
                payment.getTransactionType(), payment.getDescription());

        // Update payment status
        payment.setStatus("LEDGER_UPDATED");
        payment.setApprovedAt(LocalDateTime.now());
        payment.setApprovedBy(approvedBy);
        paymentApprovalRepository.save(payment);
    }

    public void rejectPayment(Long paymentId, Long rejectedBy, String reason) {
        PaymentApproval payment = paymentApprovalRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!"PAYMENT_ADDED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment already processed");
        }

        // Update payment status
        payment.setStatus("PAYMENT_REJECTED");
        payment.setApprovedAt(LocalDateTime.now());
        payment.setApprovedBy(rejectedBy);
        payment.setRejectionReason(reason);
        paymentApprovalRepository.save(payment);
    }

    public List<PaymentApproval> getPendingPayments(Long distributorId) {
        List<PaymentApproval> payments;
        if (distributorId != null) {
            payments = paymentApprovalRepository.findByDistributorIdAndStatusOrderByCreatedAtDesc(distributorId, "PAYMENT_ADDED");
        } else {
            payments = paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("PAYMENT_ADDED");
        }

        // Populate distributor names
        payments.forEach(payment -> {
            distributorRepository.findById(payment.getDistributorId())
                    .ifPresent(distributor -> payment.setDistributorName(distributor.getFirstName()));
        });

        return payments;
    }

    public List<PaymentApproval> getAllPendingPayments() {
        List<PaymentApproval> payments = paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("PAYMENT_ADDED");

        // Populate distributor names
        payments.forEach(payment -> {
            distributorRepository.findById(payment.getDistributorId())
                    .ifPresent(distributor -> payment.setDistributorName(distributor.getFirstName()));
        });

        return payments;
    }

    public List<PaymentApproval> getPendingPaymentsBySalesperson(Long salespersonId) {
        List<PaymentApproval> allPendingPayments = paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("PAYMENT_ADDED");
        
        // Filter by salespersonId
        List<PaymentApproval> payments = allPendingPayments.stream()
                .filter(payment -> salespersonId.equals(payment.getSalespersonId()))
                .collect(java.util.stream.Collectors.toList());

        // Populate distributor names
        payments.forEach(payment -> {
            distributorRepository.findById(payment.getDistributorId())
                    .ifPresent(distributor -> payment.setDistributorName(distributor.getFirstName()));
        });

        return payments;
    }

    public List<DistributorLedger> getJournalVouchersByDistributor(Long distributorId) {
        return distributorLedgerRepository.findByDistributorIdAndTransactionTypeOrderByCreatedAtDesc(distributorId, "JV");
    }

    public void processJournalVoucher(Long distributorId, JournalVoucherRequest request) {
        for (JournalVoucherEntry entry : request.getEntries()) {
            if (entry.getDebit() != null && entry.getDebit().compareTo(BigDecimal.ZERO) > 0) {
                distributorRepository.findById(distributorId).ifPresentOrElse(
                        distributor -> {
                            if (!entry.getAccountNumber().equals(distributor.getAccountNumber())) {
                                throw new RuntimeException("Account number " + entry.getAccountNumber() + " does not match distributor's account number " + distributor.getAccountNumber());
                            }
                        },
                        () -> {
                            throw new RuntimeException("Distributor not found with ID: " + distributorId);
                        }
                );
                updateDistributorBalance(distributorId, entry.getDebit(), "JV",
                        entry.getDescription() + " - " + request.getNarration());
            }

            if (entry.getCredit() != null && entry.getCredit().compareTo(BigDecimal.ZERO) > 0) {
                distributorRepository.findById(distributorId).ifPresentOrElse(
                        distributor -> {
                            if (!entry.getAccountNumber().equals(distributor.getAccountNumber())) {
                                throw new RuntimeException("Account number " + entry.getAccountNumber() + " does not match distributor's account number " + distributor.getAccountNumber());
                            }
                        },
                        () -> {
                            throw new RuntimeException("Distributor not found with ID: " + distributorId);
                        }
                );
                updateDistributorBalance(distributorId, entry.getCredit(), "JV",
                        entry.getDescription() + " - " + request.getNarration());
            }
        }
    }

    public DistributorRepository getDistributorRepository() {
        return distributorRepository;
    }

    private Long getDistributorIdFromAccountNumber(String accountNumber) {
        // You'll need to implement this logic based on how account numbers map to distributor IDs
        // This could be a repository call or a simple parsing if account numbers contain the ID
        try {
            return Long.parseLong(accountNumber);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid account number: " + accountNumber);
        }
    }

    // Order Tracking Integration Methods
    
    /**
     * Updates order tracking step when Proforma Invoice is generated
     * Step 4: Proforma Invoice Generated
     */
    private void updatePIGenerationStep(Long orderId) {
        try {
            // Find order tracking by order number (assuming orderId is cartId that maps to orderNumber)
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByOrderNumber(orderNumber);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus("completed");
                request.setRemarks("Proforma Invoice generated successfully");
                request.setDate(java.time.LocalDate.now().toString());
                request.setHasDownload(true);
                request.setDownloadLabel("Download Proforma Invoice");
                
                // Add assigned person (accounts team) information
                setCurrentUserDetails(request);
                request.setAssignedPersonRole("ACCOUNTS_MANAGER");
                
                orderTrackingService.updateStepBySequence(order.getId(), 4, request);
            } else {
                System.err.println("Order tracking not found for order number: " + orderNumber);
            }
        } catch (Exception e) {
            // Log error but don't fail the main process
            System.err.println("Failed to update PI generation step: " + e.getMessage());
        }
    }

    /**
     * Updates order tracking step when awaiting payment confirmation
     * Step 5: Awaiting Payment Confirmation from Accounts
     */
    private void updateAwaitingPaymentStep(Long orderId) {
        try {
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByOrderNumber(orderNumber);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus("in_progress");
                request.setRemarks("Awaiting payment confirmation from accounts");
                request.setDate(java.time.LocalDate.now().toString());
                
                // Add assigned person (accounts team) information
                setCurrentUserDetails(request);
                request.setAssignedPersonRole("ACCOUNTS_MANAGER");
                
                orderTrackingService.updateStepBySequence(order.getId(), 5, request);
            } else {
                System.err.println("Order tracking not found for order number: " + orderNumber);
            }
        } catch (Exception e) {
            System.err.println("Failed to update awaiting payment step: " + e.getMessage());
        }
    }

    /**
     * Updates order tracking step when payment is approved by accounts
     * Step 6: Approved from Accounts
     */
    private void updatePaymentApprovedStep(Long orderId) {
        try {
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByOrderNumber(orderNumber);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus("completed");
                request.setRemarks("Payment approved by accounts team");
                request.setDate(java.time.LocalDate.now().toString());
                
                // Add assigned person (accounts team) information
                setCurrentUserDetails(request);
                request.setAssignedPersonRole("ACCOUNTS_MANAGER");
                
                orderTrackingService.updateStepBySequence(order.getId(), 6, request);
                
                // Also explicitly complete Step 5 (Awaiting Payment Confirmation) if it was IN_PROGRESS
                UpdateStepRequest step5Request = new UpdateStepRequest();
                step5Request.setStatus("completed");
                step5Request.setRemarks("Payment confirmed and approved");
                step5Request.setDate(java.time.LocalDate.now().toString());
                setCurrentUserDetails(step5Request);
                step5Request.setAssignedPersonRole("ACCOUNTS_MANAGER");
                
                try {
                    orderTrackingService.updateStepBySequence(order.getId(), 5, step5Request);
                } catch (Exception e) {
                    // Step 5 might already be completed, log but don't fail
                    System.err.println("Step 5 already completed or not found: " + e.getMessage());
                }
            } else {
                System.err.println("Order tracking not found for order number: " + orderNumber);
            }
        } catch (Exception e) {
            System.err.println("Failed to update payment approved step: " + e.getMessage());
        }
    }

    /**
     * Ensures order tracking exists for the given order, creates if missing
     */
    private void ensureOrderTrackingExists(Long orderId) {
        try {
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            com.nector.userservice.ordertracking.entity.OrderTracking existingOrder = 
                orderTrackingService.getOrderRepository().findByOrderNumber(orderNumber);
            
            if (existingOrder == null) {
                // Create order tracking if it doesn't exist
                createOrderTrackingFromCart(orderId);
            }
        } catch (Exception e) {
            System.err.println("Error ensuring order tracking exists: " + e.getMessage());
        }
    }

    /**
     * Creates OrderTracking entry when cart is approved/converted to order
     * This should be called when cart status changes to APPROVED
     */
    public void createOrderTrackingFromCart(Long cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found: " + cartId));
            
            // Get distributor info
            String distributorName = distributorRepository.findById(cart.getDistributorId())
                .map(distributor -> distributor.getFirstName() + " " + distributor.getLastName())
                .orElse("Unknown Distributor");
            
            // Calculate total amount
            BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Create order number
            String orderNumber = "ORD-" + cartId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // Use OrderTrackingService to create from cart
            orderTrackingService.createFromCart(
                cartId, 
                distributorName, 
                cart.getDistributorId(), 
                orderNumber, 
                totalAmount
            );
            
        } catch (Exception e) {
            System.err.println("Failed to create order tracking from cart: " + e.getMessage());
        }
    }

    /**
     * Generic method to update order tracking steps
     */
    private void updateOrderTrackingStep(Long orderId, Integer stepSequence, String status, String remarks) {
        try {
            // Find the order tracking entry by order number
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByOrderNumber(orderNumber);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus(status);
                request.setRemarks(remarks);
                request.setDate(java.time.LocalDate.now().toString());
                
                orderTrackingService.updateStepBySequence(order.getId(), stepSequence.intValue(), request);
            } else {
                System.err.println("Order tracking not found for order number: " + orderNumber);
            }
        } catch (Exception e) {
            System.err.println("Error updating order tracking step: " + e.getMessage());
        }
    }


}
