package com.nector.userservice.service;

import com.nector.userservice.dto.payment.*;
import com.nector.userservice.interceptors.accounts.model.PaymentHistoryResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
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
            updateOrderTrackingStep(orderId, 4, "completed", "Proforma Invoice generated successfully");
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
            updateOrderTrackingStep(orderId, 5, "in_progress", "Awaiting payment confirmation from accounts");
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
            updateOrderTrackingStep(orderId, 6, "completed", "Payment approved by accounts team");
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
