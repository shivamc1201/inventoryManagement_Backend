package com.nector.userservice.service;

import com.nector.userservice.dto.payment.PaymentRequest;
import com.nector.userservice.dto.payment.PaymentResponse;
import com.nector.userservice.model.PaymentApproval;
import com.nector.userservice.model.ProformaInvoice;
import com.nector.userservice.model.DistributorLedger;
import com.nector.userservice.model.Cart;
import com.nector.userservice.repository.*;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.interceptors.salesMapping.model.MappingStatus;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
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
    
    public com.nector.userservice.dto.payment.OrderApprovalResponse checkAndApproveOrder(Long orderId, Long distributorId) {
        // Get cart and calculate total amount
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Cart not found: " + orderId));
        
        java.math.BigDecimal orderAmount = cart.getCartItems().stream()
            .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        java.math.BigDecimal ledgerBalance = getDistributorBalance(distributorId);
        
        com.nector.userservice.dto.payment.OrderApprovalResponse response = new com.nector.userservice.dto.payment.OrderApprovalResponse();
        response.setOrderId(orderId);
        response.setDistributorId(distributorId);
        response.setOrderAmount(orderAmount);
        response.setLedgerBalance(ledgerBalance);
        
        if (ledgerBalance.compareTo(orderAmount) >= 0) {
            response.setStatus("APPROVED");
            response.setMessage("Order approved - sufficient balance");
            // Deduct amount from distributor balance
            updateDistributorLedger(distributorId, orderAmount.negate());
            // Update PI payment status
            updateProformaInvoiceStatus(orderId, ProformaInvoice.PaymentStatus.PAID);
            // Update Cart status to PAYMENT_APPROVED
            cart.setStatus(Cart.CartStatus.PAYMENT_APPROVED);
            cartRepository.save(cart);
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

    public List<PaymentApproval> getPendingPayments() {
        return paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("PAYMENT_ADDED");
    }
}