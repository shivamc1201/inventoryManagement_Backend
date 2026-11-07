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
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import com.nector.userservice.ordertracking.dto.UpdateStepRequest;
import com.nector.userservice.ordertracking.dto.CreateOrderTrackingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;

@Service
@Slf4j
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
    private DocumentNumberService documentNumberService;
    
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

        // Idempotency guard: prevent double-deduction if already approved
        if (cart.getStatus() == Cart.CartStatus.PAYMENT_APPROVED) {
            OrderApprovalResponse response = new OrderApprovalResponse();
            response.setOrderId(orderId);
            response.setDistributorId(distributorId);
            response.setStatus("ALREADY_APPROVED");
            response.setMessage("Order is already payment approved — no deduction made");
            log.warn("checkAndApproveOrder called on already-approved cart {}, skipping deduction", orderId);
            return response;
        }

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
            .filter(distributor -> salespersonId.equals(distributor.getSalespersonId()))
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
    
    public void updateDistributorBalance(Long distributorId, BigDecimal amount, String transactionType, String description, LocalDateTime date) {
        DistributorLedger ledger = new DistributorLedger();
        ledger.setDistributorId(distributorId);
        ledger.setAmount(amount);
        ledger.setTransactionType(transactionType);
        ledger.setDescription(description);
        ledger.setCreatedAt(date != null ? date : LocalDateTime.now());
        distributorLedgerRepository.save(ledger);
    }

    // For direct credit entries: credit is a loan — restore it first, then credit the ledger.
    public void updateDistributorBalanceWithCreditSplit(Long distributorId, BigDecimal amount, String transactionType, String description, LocalDateTime date) {
        if (!"CREDIT".equalsIgnoreCase(transactionType)) {
            updateDistributorBalance(distributorId, amount, transactionType, description, date);
            return;
        }

        var distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new RuntimeException("Distributor not found: " + distributorId));

        BigDecimal creditLimit = distributor.getCreditLimit() != null ? distributor.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal creditBalance = distributor.getCreditBalance() != null ? distributor.getCreditBalance() : BigDecimal.ZERO;

        BigDecimal creditUsed = creditLimit.subtract(creditBalance);
        BigDecimal creditRestored = amount.min(creditUsed);
        BigDecimal ledgerPortion = amount.subtract(creditRestored);

        if (creditRestored.compareTo(BigDecimal.ZERO) > 0) {
            distributor.setCreditBalance(creditBalance.add(creditRestored));
            distributorRepository.save(distributor);
            updateDistributorBalance(distributorId, creditRestored, "CREDIT", description + " (Credit Restored)", date);
        }
        if (ledgerPortion.compareTo(BigDecimal.ZERO) > 0) {
            updateDistributorBalance(distributorId, ledgerPortion, "CREDIT", description + " (Ledger Balance)", date);
        }
    }
    
    public List<DistributorLedger> getPaymentHistory(Long distributorId) {
        return distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
    }
    
    public PaymentHistoryResponse getPaymentHistoryWithBalance(Long distributorId) {
        List<DistributorLedger> paymentHistory = distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);

        BigDecimal totalCredits = paymentHistory.stream()
                .filter(e -> isCreditType(e.getTransactionType()))
                .map(DistributorLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebits = paymentHistory.stream()
                .filter(e -> isDebitType(e.getTransactionType()))
                .map(DistributorLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal closingBalance = totalCredits.subtract(totalDebits);
        BigDecimal netBalance = computeBalanceFromLastZeroPoint(distributorId);

        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setPaymentHistory(paymentHistory);
        response.setTotalCredits(totalCredits);
        response.setTotalDebits(totalDebits);
        response.setClosingBalance(closingBalance);
        response.setNetBalance(netBalance);
        response.setDistributorId(distributorId);

        return response;
    }
    
    public PaymentHistoryWithRunningBalanceResponse getPaymentHistoryWithRunningBalance(Long distributorId) {
        List<DistributorLedger> paymentHistory = distributorLedgerRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
        BigDecimal closingBalance = computeBalanceFromLastZeroPoint(distributorId);

        List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> paymentHistoryWithBalance =
            calculateRunningBalance(paymentHistory);

        PaymentHistoryWithRunningBalanceResponse response = new PaymentHistoryWithRunningBalanceResponse();
        response.setPaymentHistory(paymentHistoryWithBalance);
        response.setClosingBalance(closingBalance);
        response.setDistributorId(distributorId);

        return response;
    }
    
    private List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> calculateRunningBalance(
            List<DistributorLedger> paymentHistory) {

        List<PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance> result = new ArrayList<>();

        // Input is newest-first; reverse to oldest-first for forward accumulation.
        List<DistributorLedger> chronological = new ArrayList<>(paymentHistory);
        Collections.reverse(chronological);

        BigDecimal runningBalance = BigDecimal.ZERO;

        for (DistributorLedger transaction : chronological) {
            if (isDebitType(transaction.getTransactionType())) {
                runningBalance = runningBalance.subtract(transaction.getAmount());
            } else if (isCreditType(transaction.getTransactionType())) {
                runningBalance = runningBalance.add(transaction.getAmount());
            }

            PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance item =
                new PaymentHistoryWithRunningBalanceResponse.PaymentHistoryWithBalance();
            item.setId(transaction.getId());
            item.setAmount(transaction.getAmount());
            item.setCreatedAt(transaction.getCreatedAt());
            item.setDescription(transaction.getDescription());
            item.setDistributorId(transaction.getDistributorId());
            item.setTransactionType(transaction.getTransactionType());
            item.setCurrentCB(runningBalance);

            result.add(item);
        }

        // Return newest-first for display.
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
            updateDistributorBalance(distributorId, piAmount, "DEBIT", "Payment for Order #" + orderId, LocalDateTime.now());
            
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
    
    @Transactional
    public com.nector.userservice.dto.payment.OrderApprovalResponse approvePIUsingCredit(Long orderId, Long distributorId) {
        // Get PI for the order
        ProformaInvoice pi = proformaInvoiceRepository.findByCartId(orderId)
            .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for order: " + orderId));

        // Idempotency guard: prevent double-deduction if PI already paid
        if (pi.getPaymentStatus() == ProformaInvoice.PaymentStatus.PAID) {
            throw new RuntimeException("Order #" + orderId + " is already paid. Cannot approve PI again.");
        }

        // Row-level lock on the distributor to serialize concurrent credit approvals.
        var distributor = distributorRepository.findByIdForUpdate(distributorId)
            .orElseThrow(() -> new RuntimeException("Distributor not found: " + distributorId));

        BigDecimal currentCreditBalance = distributor.getCreditBalance();
        BigDecimal orderAmount = pi.getAmount();

        if (currentCreditBalance == null) {
            throw new RuntimeException("Credit balance not available for distributor: " + distributorId);
        }

        if (currentCreditBalance.compareTo(orderAmount) < 0) {
            throw new RuntimeException("Insufficient credit balance. Available: " + currentCreditBalance + ", Required: " + orderAmount);
        }

        // Ensure order tracking exists before updating steps
        ensureOrderTrackingExists(orderId);

        // Update order tracking Step 5: Awaiting Payment Confirmation
        updateAwaitingPaymentStep(orderId);

        // Update PI status to PAID directly (using credit)
        pi.setPaymentStatus(ProformaInvoice.PaymentStatus.PAID);
        proformaInvoiceRepository.save(pi);

        // Update Cart status to PAYMENT_APPROVED
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        cart.setStatus(Cart.CartStatus.PAYMENT_APPROVED);
        cartRepository.save(cart);

        // Deduct amount from distributor's creditBalance
        distributor.setCreditBalance(currentCreditBalance.subtract(orderAmount));
        distributorRepository.save(distributor);

        // Create a ledger entry for audit trail
        updateDistributorBalance(distributorId, orderAmount, "DEBIT", "Payment for Order #" + orderId + " (using credit)", LocalDateTime.now());

        // Update order tracking Step 6: Approved from Accounts
        updatePaymentApprovedStep(orderId);

        BigDecimal updatedCreditBalance = distributor.getCreditBalance();

        com.nector.userservice.dto.payment.OrderApprovalResponse response = new com.nector.userservice.dto.payment.OrderApprovalResponse();
        response.setOrderId(orderId);
        response.setDistributorId(distributorId);
        response.setOrderAmount(orderAmount);
        response.setLedgerBalance(updatedCreditBalance);
        response.setStatus("APPROVED_USING_CREDIT");
        response.setMessage("PI approved using available credit balance");

        return response;
    }

    /**
     * Unified order approval: combines ledger balance + credit balance.
     * Allocation order: ledger first (advance deposits), credit as fallback.
     *
     * Ledger entry description encodes the exact split so the statement is self-auditable:
     *   - Ledger only  → "Payment for Order #X (Ledger Balance)"
     *   - Credit only  → "Payment for Order #X (using credit)"
     *   - Combined     → "Payment for Order #X (Ledger ₹50000 + Credit ₹78500)"
     *
     * Always acquires a PESSIMISTIC_WRITE lock on the Distributor row to prevent
     * concurrent approvals from double-spending either balance pool.
     */
    @Transactional
    public UnifiedOrderApprovalResponse approveOrderUnified(Long orderId) {
        Cart cart = cartRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Long distributorId = cart.getDistributorId();

        UnifiedOrderApprovalResponse response = new UnifiedOrderApprovalResponse();
        response.setOrderId(orderId);
        response.setDistributorId(distributorId);

        // Idempotency guard on cart status
        if (cart.getStatus() == Cart.CartStatus.PAYMENT_APPROVED) {
            response.setStatus("ALREADY_APPROVED");
            response.setMessage("Order " + orderId + " is already payment approved — no deduction made");
            log.warn("approveOrderUnified: order {} already PAYMENT_APPROVED, skipping", orderId);
            return response;
        }

        ProformaInvoice pi = proformaInvoiceRepository.findByCartId(orderId)
                .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for order: " + orderId));

        // Idempotency guard on PI status
        if (pi.getPaymentStatus() == ProformaInvoice.PaymentStatus.PAID) {
            response.setStatus("ALREADY_APPROVED");
            response.setMessage("PI for order " + orderId + " is already PAID — no deduction made");
            log.warn("approveOrderUnified: PI for order {} already PAID, skipping", orderId);
            return response;
        }

        BigDecimal orderAmount = pi.getAmount();
        response.setOrderAmount(orderAmount);

        // Row-level lock on distributor — serializes all concurrent approvals for this distributor
        // so neither ledger balance nor creditBalance can be double-spent.
        var distributor = distributorRepository.findByIdForUpdate(distributorId)
                .orElseThrow(() -> new RuntimeException("Distributor not found: " + distributorId));

        // Available balances
        BigDecimal ledgerBalance = computeBalanceFromLastZeroPoint(distributorId);
        BigDecimal effectiveLedger = ledgerBalance.max(BigDecimal.ZERO); // negative ledger = ₹0 contribution
        BigDecimal creditBalance = distributor.getCreditBalance() != null
                ? distributor.getCreditBalance() : BigDecimal.ZERO;
        BigDecimal combinedAvailable = effectiveLedger.add(creditBalance);

        log.info("approveOrderUnified: order={}, amount={}, effectiveLedger={}, creditBalance={}, combined={}",
                orderId, orderAmount, effectiveLedger, creditBalance, combinedAvailable);

        if (combinedAvailable.compareTo(orderAmount) < 0) {
            BigDecimal shortfall = orderAmount.subtract(combinedAvailable);
            response.setStatus("PENDING");
            response.setMessage("Insufficient balance — required ₹" + orderAmount
                    + ", available ₹" + combinedAvailable + " (shortfall ₹" + shortfall + ")");
            response.setAvailableLedgerBalance(effectiveLedger);
            response.setAvailableCreditBalance(creditBalance);
            response.setCombinedAvailableBalance(combinedAvailable);
            response.setShortfall(shortfall);
            log.warn("approveOrderUnified: REJECTED order={} — shortfall={} (ledger={}, credit={})",
                    orderId, shortfall, effectiveLedger, creditBalance);
            return response;
        }

        // Allocate: ledger first, credit for the remainder
        BigDecimal ledgerUsed  = effectiveLedger.min(orderAmount);
        BigDecimal creditUsed  = orderAmount.subtract(ledgerUsed);

        // Determine ledger entry description
        String ledgerDescription;
        if (creditUsed.compareTo(BigDecimal.ZERO) == 0) {
            ledgerDescription = "Payment for Order #" + orderId + " (Ledger Balance)";
        } else if (ledgerUsed.compareTo(BigDecimal.ZERO) == 0) {
            ledgerDescription = "Payment for Order #" + orderId + " (using credit)";
        } else {
            ledgerDescription = String.format(
                    "Payment for Order #%d (Ledger ₹%s + Credit ₹%s)",
                    orderId,
                    ledgerUsed.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString(),
                    creditUsed.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString());
        }

        // Single DEBIT ledger entry for the full order amount — description encodes the split
        updateDistributorBalance(distributorId, orderAmount, "DEBIT", ledgerDescription, LocalDateTime.now());

        // Deduct credit portion from the entity field
        if (creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            distributor.setCreditBalance(creditBalance.subtract(creditUsed));
            distributorRepository.save(distributor);
        }

        // Update PI and cart
        pi.setPaymentStatus(ProformaInvoice.PaymentStatus.PAID);
        proformaInvoiceRepository.save(pi);
        cart.setStatus(Cart.CartStatus.PAYMENT_APPROVED);
        cartRepository.save(cart);

        // Order tracking
        ensureOrderTrackingExists(orderId);
        updateAwaitingPaymentStep(orderId);
        updatePaymentApprovedStep(orderId);

        // Build success response
        BigDecimal remainingLedger = ledgerBalance.subtract(ledgerUsed);
        BigDecimal remainingCredit = creditBalance.subtract(creditUsed);

        response.setStatus("APPROVED");
        response.setLedgerBalanceUsed(ledgerUsed);
        response.setCreditBalanceUsed(creditUsed);
        response.setRemainingLedgerBalance(remainingLedger);
        response.setRemainingCreditBalance(remainingCredit);

        if (creditUsed.compareTo(BigDecimal.ZERO) == 0) {
            response.setMessage("Order approved using ledger balance");
        } else if (ledgerUsed.compareTo(BigDecimal.ZERO) == 0) {
            response.setMessage("Order approved using credit balance");
        } else {
            response.setMessage(String.format(
                    "Order approved using combined balance (Ledger ₹%s + Credit ₹%s)",
                    ledgerUsed.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString(),
                    creditUsed.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString()));
        }

        log.info("approveOrderUnified: APPROVED order={} — amount={}, ledgerUsed={}, creditUsed={}, remainingLedger={}, remainingCredit={}",
                orderId, orderAmount, ledgerUsed, creditUsed, remainingLedger, remainingCredit);

        return response;
    }

    /**
     * Adds credit amount to a distributor's credit balance.
     * This increases the distributor's creditBalance but cannot exceed creditLimit.
     * @param distributorId  the distributor to credit
     * @param amount         how much credit to add (must be > 0)
     * @param description    reason / remarks for the credit addition
     */
    @Transactional
    public void addCreditToDistributor(Long distributorId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero");
        }

        var distributor = distributorRepository.findByIdForUpdate(distributorId)
            .orElseThrow(() -> new RuntimeException("Distributor not found: " + distributorId));

        BigDecimal currentCreditBalance = distributor.getCreditBalance() != null
            ? distributor.getCreditBalance()
            : BigDecimal.ZERO;

        BigDecimal creditLimit = distributor.getCreditLimit() != null
            ? distributor.getCreditLimit()
            : BigDecimal.ZERO;

        BigDecimal newCreditBalance = currentCreditBalance.add(amount);

        // Check if new balance would exceed credit limit
        if (newCreditBalance.compareTo(creditLimit) > 0) {
            throw new IllegalArgumentException(
                String.format("Cannot add credit. New balance (%s) would exceed credit limit (%s). Current balance: %s, Amount to add: %s",
                    newCreditBalance, creditLimit, currentCreditBalance, amount)
            );
        }

        distributor.setCreditBalance(newCreditBalance);
        distributorRepository.save(distributor);

        // Create a ledger entry so the transaction appears in history. Tagged '(Credit Restored)'
        // to identify it as a credit-line refill rather than a direct cash payment.
        updateDistributorBalance(distributorId, amount, "CREDIT",
                description + " (Credit Restored)", LocalDateTime.now());
    }

    public List<ProformaInvoice> getPendingPaymentApprovals() {
        return proformaInvoiceRepository.findAll().stream()
            .filter(pi -> pi.getPaymentStatus() == ProformaInvoice.PaymentStatus.PENDING)
            .toList();
    }
    
    private java.math.BigDecimal getDistributorBalance(Long distributorId) {
        return computeBalanceFromLastZeroPoint(distributorId);
    }
    
    private void updateDistributorLedger(Long distributorId, java.math.BigDecimal amount) {
        String transactionType = amount.compareTo(BigDecimal.ZERO) > 0 ? "CREDIT" : "DEBIT";
        String description = amount.compareTo(BigDecimal.ZERO) > 0 ? "Payment received" : "Order deduction";
        updateDistributorBalance(distributorId, amount.abs(), transactionType, description, LocalDateTime.now());
    }

    public Long addPaymentForApproval(Long distributorId, BigDecimal amount, String transactionType, String description, LocalDateTime date,
                                       String utrTxnNo, String refNo, String chequeNo) {
        PaymentApproval payment = new PaymentApproval();
        payment.setDistributorId(distributorId);
        payment.setAmount(amount);
        payment.setTransactionType(transactionType);
        payment.setDescription(description);
        payment.setStatus("PAYMENT_ADDED");
        payment.setCreatedAt(date != null ? date : LocalDateTime.now());
        payment.setUtrTxnNo(utrTxnNo);
        payment.setRefNo(refNo);
        payment.setChequeNo(chequeNo);
        PaymentApproval savedPayment = paymentApprovalRepository.save(payment);
        return savedPayment.getId();
    }

    public Long addPaymentForApprovalWithSalesperson(Long distributorId, Long salespersonId, BigDecimal amount, String transactionType, String description, LocalDateTime date,
                                                      String utrTxnNo, String refNo, String chequeNo) {
        PaymentApproval payment = new PaymentApproval();
        payment.setDistributorId(distributorId);
        payment.setSalespersonId(salespersonId);
        payment.setAmount(amount);
        payment.setTransactionType(transactionType);
        payment.setDescription(description);
        payment.setStatus("PAYMENT_ADDED");
        payment.setCreatedAt(date != null ? date : LocalDateTime.now());
        payment.setUtrTxnNo(utrTxnNo);
        payment.setRefNo(refNo);
        payment.setChequeNo(chequeNo);
        PaymentApproval savedPayment = paymentApprovalRepository.save(payment);
        return savedPayment.getId();
    }

    @Transactional
    public void approvePayment(Long paymentId, Long approvedBy) {
        PaymentApproval payment = paymentApprovalRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!"PAYMENT_ADDED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment already processed");
        }

        // For CREDIT transactions: incoming payment first restores credit line (up to limit),
        // any excess becomes ledger balance. Both are logged in distributor_ledger for history,
        // Both portions are recorded in distributor_ledger for full history.
        if ("CREDIT".equalsIgnoreCase(payment.getTransactionType())) {
            var distributor = distributorRepository.findByIdForUpdate(payment.getDistributorId())
                    .orElseThrow(() -> new RuntimeException("Distributor not found: " + payment.getDistributorId()));

            BigDecimal creditLimit = distributor.getCreditLimit() != null ? distributor.getCreditLimit() : BigDecimal.ZERO;
            BigDecimal creditBalance = distributor.getCreditBalance() != null ? distributor.getCreditBalance() : BigDecimal.ZERO;
            BigDecimal paymentAmount = payment.getAmount();

            BigDecimal shortfall = creditLimit.subtract(creditBalance).max(BigDecimal.ZERO);
            BigDecimal creditRestore = paymentAmount.min(shortfall);
            BigDecimal ledgerCredit = paymentAmount.subtract(creditRestore);

            if (creditRestore.compareTo(BigDecimal.ZERO) > 0) {
                distributor.setCreditBalance(creditBalance.add(creditRestore));
                distributorRepository.save(distributor);

                updateDistributorBalance(payment.getDistributorId(), creditRestore,
                        "CREDIT", payment.getDescription() + " (Credit Restored)", payment.getCreatedAt());
            }

            if (ledgerCredit.compareTo(BigDecimal.ZERO) > 0) {
                updateDistributorBalance(payment.getDistributorId(), ledgerCredit,
                        "CREDIT", payment.getDescription() + " (Ledger Balance)", payment.getCreatedAt());
            }
        } else {
            // For DEBIT or other transaction types, process normally
            updateDistributorBalance(payment.getDistributorId(), payment.getAmount(),
                    payment.getTransactionType(), payment.getDescription(), payment.getCreatedAt());
        }

        // Update payment status
        payment.setStatus("LEDGER_UPDATED");
        payment.setApprovedAt(payment.getCreatedAt());
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
        return distributorLedgerRepository.findByDistributorIdAndTransactionTypeInOrderByCreatedAtDesc(
                distributorId, java.util.List.of("JV_CREDIT", "JV_DEBIT"));
    }

    public List<PaymentApproval> getLedgerUpdatedPayments(Long salespersonId, Long distributorId) {
        List<PaymentApproval> payments;
        
        if (salespersonId != null && distributorId != null) {
            // Both parameters provided
            payments = paymentApprovalRepository.findByDistributorIdAndSalespersonIdAndStatusOrderByCreatedAtDesc(
                    distributorId, salespersonId, "LEDGER_UPDATED");
        } else if (salespersonId != null) {
            // Only salespersonId provided
            payments = paymentApprovalRepository.findBySalespersonIdAndStatusOrderByCreatedAtDesc(
                    salespersonId, "LEDGER_UPDATED");
        } else if (distributorId != null) {
            // Only distributorId provided
            payments = paymentApprovalRepository.findByDistributorIdAndStatusOrderByCreatedAtDesc(
                    distributorId, "LEDGER_UPDATED");
        } else {
            // No filters - get all LEDGER_UPDATED payments
            payments = paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("LEDGER_UPDATED");
        }

        // Populate distributor names
        payments.forEach(payment -> {
            if (payment.getDistributorName() == null) {
                distributorRepository.findById(payment.getDistributorId())
                        .ifPresent(distributor -> payment.setDistributorName(distributor.getFirstName()));
            }
        });

        return payments;
    }

    public java.util.Map<String, BigDecimal> getCollectionAnalytics(Long distributorId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.withDayOfMonth(today.lengthOfMonth()).atTime(java.time.LocalTime.MAX);

        BigDecimal monthly = paymentApprovalRepository.sumApprovedPaymentsByDistributorAndDateRange(distributorId, monthStart, monthEnd);
        BigDecimal yearly = paymentApprovalRepository.sumAllLedgerUpdatedByDistributor(distributorId);

        java.util.Map<String, BigDecimal> result = new java.util.LinkedHashMap<>();
        result.put("collectionMonthly", monthly);
        result.put("collectionYearly", yearly);
        return result;
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
                updateDistributorBalance(distributorId, entry.getDebit(), "JV_DEBIT",
                        entry.getDescription() + " - " + request.getNarration(), request.getDate());
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
                updateDistributorBalance(distributorId, entry.getCredit(), "JV_CREDIT",
                        entry.getDescription() + " - " + request.getNarration(), request.getDate());
            }
        }
    }

    private boolean isCreditType(String transactionType) {
        return "CREDIT".equalsIgnoreCase(transactionType) || "JV_CREDIT".equalsIgnoreCase(transactionType);
    }

    private boolean isDebitType(String transactionType) {
        return "DEBIT".equalsIgnoreCase(transactionType) || "JV_DEBIT".equalsIgnoreCase(transactionType);
    }

    // Returns the net balance of all entries since the last time the running balance hit exactly zero.
    // When a distributor fully clears their account (balance == 0), that is a natural reset point.
    // The "current balance" is the net from that zero point forward, so old carry-forwards don't pollute it.
    public BigDecimal computeBalanceFromLastZeroPoint(Long distributorId) {
        List<DistributorLedger> entries = distributorLedgerRepository.findByDistributorIdOrderByCreatedAtAsc(distributorId);
        BigDecimal runningBalance = BigDecimal.ZERO;
        BigDecimal partialBalance = BigDecimal.ZERO;
        for (DistributorLedger entry : entries) {
            BigDecimal delta = isCreditType(entry.getTransactionType())
                    ? entry.getAmount()
                    : entry.getAmount().negate();
            runningBalance = runningBalance.add(delta);
            partialBalance = partialBalance.add(delta);
            if (runningBalance.compareTo(BigDecimal.ZERO) == 0) {
                partialBalance = BigDecimal.ZERO;
            }
        }
        return partialBalance;
    }

    // Rows that only move the credit line, never the ledger balance.
    // Kept in the distributor_ledger table for frontend history but excluded from balance math.
    private boolean isCreditLineOnly(DistributorLedger transaction) {
        String description = transaction.getDescription();
        if (description == null) {
            return false;
        }
        return description.contains("(using credit)") || description.contains("(Credit Restored)");
    }

    public DistributorRepository getDistributorRepository() {
        return distributorRepository;
    }

    public ProformaInvoiceRepository getProformaInvoiceRepository() {
        return proformaInvoiceRepository;
    }

    public CartRepository getCartRepository() {
        return cartRepository;
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
            // Find order tracking by cartId (orderId is actually cartId in this context)
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByCartId(orderId);
            
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
                System.err.println("Order tracking not found for cart ID: " + orderId);
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
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByCartId(orderId);
            
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
                System.err.println("Order tracking not found for cart ID: " + orderId);
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
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByCartId(orderId);
            
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
                System.err.println("Order tracking not found for cart ID: " + orderId);
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
            com.nector.userservice.ordertracking.entity.OrderTracking existingOrder = 
                orderTrackingService.getOrderRepository().findByCartId(orderId);
            
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
        log.info("Creating order tracking from cart: {}", cartId);
        try {
            Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found: " + cartId));
            
            log.info("Found cart: {}, distributorId: {}, items: {}", 
                cart.getId(), cart.getDistributorId(), cart.getCartItems().size());
            
            // Get distributor info
            String distributorName = distributorRepository.findById(cart.getDistributorId())
                .map(distributor -> distributor.getFirstName() + " " + distributor.getLastName())
                .orElse("Unknown Distributor");
            
            log.info("Distributor name: {}", distributorName);
            
            BigDecimal totalAmount = cartRepository.findTotalCartAmountById(cartId);
            if (totalAmount == null) {
                totalAmount = cart.getCartItems().stream()
                    .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                log.warn("Cart {} had no persisted totalCartAmount, computed {} from {} items",
                    cartId, totalAmount, cart.getCartItems().size());
            }
            log.info("Total amount from cart: {}", totalAmount);
            
            // Generate order number in format: SO/YYYY-YY/NNNN (e.g., SO/2026-27/0001)
            String orderNumber = documentNumberService.generateSalesOrderNumber();
            
            log.info("Generated order number: {}", orderNumber);
            
            // Use OrderTrackingService to create from cart
            orderTrackingService.createFromCart(
                cartId,
                distributorName,
                cart.getDistributorId(),
                orderNumber,
                totalAmount,
                cart.getDeliveryBy(),
                cart.getSalespersonId()
            );
            
            log.info("Successfully created order tracking for cart: {}", cartId);
            
        } catch (Exception e) {
            log.error("Failed to create order tracking from cart {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to create order tracking from cart: " + cartId, e);
        }
    }

    /**
     * Generic method to update order tracking steps
     */
    private void updateOrderTrackingStep(Long orderId, Integer stepSequence, String status, String remarks) {
        try {
            // Find the order tracking entry by cartId
            com.nector.userservice.ordertracking.entity.OrderTracking order = 
                orderTrackingService.getOrderRepository().findByCartId(orderId);
            
            if (order != null) {
                UpdateStepRequest request = new UpdateStepRequest();
                request.setStatus(status);
                request.setRemarks(remarks);
                request.setDate(java.time.LocalDate.now().toString());
                
                orderTrackingService.updateStepBySequence(order.getId(), stepSequence.intValue(), request);
            } else {
                System.err.println("Order tracking not found for cart ID: " + orderId);
            }
        } catch (Exception e) {
            System.err.println("Error updating order tracking step: " + e.getMessage());
        }
    }

    public BigDecimal getOrderAmount(Long orderId) {
        Cart cart = cartRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Cart not found: " + orderId));
        
        return cart.getCartItems().stream()
            .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCurrentLedgerBalance(Long distributorId) {
        return computeBalanceFromLastZeroPoint(distributorId);
    }

    /**
     * Updates the payment status of a Proforma Invoice
     * @param orderId The cart/order ID
     * @param paymentStatus The new payment status
     */
    private void updateProformaInvoiceStatus(Long orderId, ProformaInvoice.PaymentStatus paymentStatus) {
        ProformaInvoice pi = proformaInvoiceRepository.findByCartId(orderId)
            .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for order: " + orderId));
        
        pi.setPaymentStatus(paymentStatus);
        proformaInvoiceRepository.save(pi);
    }

    /**
     * Get credit balance information for a distributor
     * @param distributorId The distributor ID
     * @return CreditBalanceResponse with credit information
     */
    public com.nector.userservice.dto.payment.CreditBalanceResponse getCreditBalance(Long distributorId) {
        var distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new RuntimeException("Distributor not found: " + distributorId));
        
        BigDecimal creditLimit = distributor.getCreditLimit() != null ? distributor.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal creditBalance = distributor.getCreditBalance() != null ? distributor.getCreditBalance() : BigDecimal.ZERO;
        BigDecimal creditUsed = creditLimit.subtract(creditBalance);
        
        BigDecimal availablePercentage = BigDecimal.ZERO;
        if (creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            availablePercentage = creditBalance.divide(creditLimit, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        String status;
        if (creditBalance.equals(creditLimit)) {
            status = "FULL_CREDIT_AVAILABLE";
        } else if (creditBalance.compareTo(creditLimit.multiply(BigDecimal.valueOf(0.5))) >= 0) {
            status = "GOOD";
        } else if (creditBalance.compareTo(BigDecimal.ZERO) > 0) {
            status = "LOW_CREDIT";
        } else {
            status = "CREDIT_EXHAUSTED";
        }
        
        com.nector.userservice.dto.payment.CreditBalanceResponse response = new com.nector.userservice.dto.payment.CreditBalanceResponse();
        response.setDistributorId(distributorId);
        response.setDistributorName(distributor.getFirstName());
        response.setCreditLimit(creditLimit);
        response.setCreditBalance(creditBalance);
        response.setCreditUsed(creditUsed);
        response.setAvailablePercentage(availablePercentage);
        response.setStatus(status);

        return response;
    }


}
