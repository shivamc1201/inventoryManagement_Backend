package com.nector.userservice.interceptors.accounts;

import com.nector.userservice.dto.payment.*;
import com.nector.userservice.enums.TransactionType;
import com.nector.userservice.model.DistributorLedger;
import com.nector.userservice.model.PaymentApproval;
import com.nector.userservice.model.ProformaInvoice;
import com.nector.userservice.model.Cart;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.interceptors.accounts.model.PaymentHistoryResponse;
import com.nector.userservice.interceptors.accounts.model.PaymentHistoryWithRunningBalanceResponse;
import com.nector.userservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "APIs for Accounts Team management")
public class AccountsController {

    private final PaymentService paymentService;
    private final CartRepository cartRepository;
    
    @PostMapping("/process-payment")
    @Operation(summary = "Process payment", description = "Processes payment and updates distributor ledger")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        log.info("Entering processPayment() - distributorId: {}, amount: {}", paymentRequest.getDistributorId(), paymentRequest.getAmount());
        PaymentResponse processedPayment = paymentService.processPayment(paymentRequest);
        log.info("Exiting processPayment() - processed payment for distributorId: {}", paymentRequest.getDistributorId());
        return ResponseEntity.ok(processedPayment);
    }

    @Deprecated
    @PostMapping("/approve-PI/{orderId}")
    @Operation(
        summary = "[DEPRECATED] Check and approve PI (ledger only)",
        description = "Deprecated — use POST /api/order/{orderId}/approve instead. " +
                      "That endpoint automatically combines ledger + credit balance and requires no distributorId from the client.",
        deprecated = true)
    @ApiResponse(responseCode = "200", description = "Order approval status returned")
    public ResponseEntity<OrderApprovalResponse> approveOrder(
            @PathVariable Long orderId,
            @RequestParam Long distributorId) {
        log.info("Entering approveOrder() - orderId: {}, distributorId: {}", orderId, distributorId);
        if (!paymentService.isCartApproved(orderId)) {
            log.warn("Exiting approveOrder() - cart not approved for orderId: {}", orderId);
            OrderApprovalResponse response = new OrderApprovalResponse();
            response.setOrderId(orderId);
            response.setDistributorId(distributorId);
            response.setStatus("INVALID_CART");
            response.setMessage("Cart is not approved or does not exist");
            return ResponseEntity.status(400).body(response);
        }

        OrderApprovalResponse approval = paymentService.checkAndApproveOrder(orderId, distributorId);
        log.info("Exiting approveOrder() - orderId: {}, status: {}", orderId, approval.getStatus());
        return ResponseEntity.ok(approval);
    }
    
    @GetMapping("/pending-pi-payments")
    @Operation(summary = "Get all pending PI payments", description = "Retrieves all proforma invoices with pending payment status")
    @ApiResponse(responseCode = "200", description = "Pending PI payments retrieved successfully")
    public ResponseEntity<List<ProformaInvoice>> getPendingPIPayments() {
        List<ProformaInvoice> pendingPIs = paymentService.getPendingPIPayments();
        return ResponseEntity.ok(pendingPIs);
    }
    
    @Deprecated
    @PostMapping("/approve-payment/{orderId}")
    @Operation(
        summary = "[DEPRECATED] Approve payment for order (ledger only)",
        description = "Deprecated — use POST /api/order/{orderId}/approve instead. " +
                      "That endpoint automatically combines ledger + credit balance and requires no distributorId from the client.",
        deprecated = true)
    @ApiResponse(responseCode = "200", description = "Payment approved successfully")
    public ResponseEntity<OrderApprovalResponse> approvePayment(
            @PathVariable Long orderId,
            @RequestParam Long distributorId) {
        log.info("Entering approvePayment() - orderId: {}, distributorId: {}", orderId, distributorId);
        OrderApprovalResponse approval = paymentService.approvePaymentForOrder(orderId, distributorId);
        log.info("Exiting approvePayment() - orderId: {}, status: {}", orderId, approval.getStatus());
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/add-credit/{distributorId}")
    @Operation(summary = "Add credit to distributor", description = "Increases the credit limit (creditAmount) for a distributor")
    @ApiResponse(responseCode = "200", description = "Credit added successfully")
    public ResponseEntity<PaymentApprovalResponse> addCreditToDistributor(
            @PathVariable Long distributorId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Credit added by accounts team") String description) {
        log.info("Entering addCreditToDistributor() - distributorId: {}, amount: {}", distributorId, amount);
        paymentService.addCreditToDistributor(distributorId, amount, description);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(null);
        response.setMessage("Credit of " + amount + " added successfully to distributor " + distributorId);
        response.setStatus("CREDIT_ADDED");
        log.info("Exiting addCreditToDistributor() - added credit {} to distributorId: {}", amount, distributorId);
        return ResponseEntity.ok(response);
    }

    @Deprecated
    @PostMapping("/approve-PI-using-credit/{orderId}")
    @Operation(
        summary = "[DEPRECATED] Approve PI using credit balance only",
        description = "Deprecated — use POST /api/order/{orderId}/approve instead. " +
                      "That endpoint automatically combines ledger + credit balance and requires no distributorId from the client.",
        deprecated = true)
    @ApiResponse(responseCode = "200", description = "PI approved using credit successfully")
    public ResponseEntity<OrderApprovalResponse> approvePIUsingCredit(
            @PathVariable Long orderId,
            @RequestParam Long distributorId) {
        OrderApprovalResponse approval = paymentService.approvePIUsingCredit(orderId, distributorId);
        return ResponseEntity.ok(approval);
    }
    
    @GetMapping("/pending-payment-approvals")
    @Operation(summary = "Get orders pending payment approval", description = "Retrieves all approved orders waiting for payment approval")
    @ApiResponse(responseCode = "200", description = "Pending payment approvals retrieved successfully")
    public ResponseEntity<List<ProformaInvoice>> getPendingPaymentApprovals() {
        List<ProformaInvoice> pendingPayments = paymentService.getPendingPaymentApprovals();
        return ResponseEntity.ok(pendingPayments);
    }
    
    @GetMapping("/payment-history/{distributorId}")
    @Operation(summary = "Get payment history", description = "Retrieves payment history for a distributor with closing balance")
    @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully")
    public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(@PathVariable Long distributorId) {
        PaymentHistoryResponse response = paymentService.getPaymentHistoryWithBalance(distributorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-history-with-balance/{distributorId}")
    @Operation(summary = "Get payment history with running balance", description = "Retrieves payment history for a distributor with running balance for each transaction")
    @ApiResponse(responseCode = "200", description = "Payment history with running balance retrieved successfully")
    public ResponseEntity<PaymentHistoryWithRunningBalanceResponse> getPaymentHistoryWithRunningBalance(@PathVariable Long distributorId) {
        PaymentHistoryWithRunningBalanceResponse response = paymentService.getPaymentHistoryWithRunningBalance(distributorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-balance")
    @Operation(
        summary = "Add payment for approval",
        description = "Submit a payment entry for approval. " +
                      "paymentMode: RTGS/NEFT → provide utrNumber | CHEQUE → provide chequeNumber + bankName | IMPS/UPI → provide transactionNumber | CASH → no reference needed.")
    @ApiResponse(responseCode = "200", description = "Payment added for approval successfully")
    public ResponseEntity<PaymentApprovalResponse> updateBalance(
            @Valid @RequestBody PaymentSubmissionRequest request) {
        Long paymentId = paymentService.addPaymentFromRequest(request);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(paymentId);
        response.setMessage("Payment added for approval");
        response.setStatus("PAYMENT_ADDED");

        return ResponseEntity.ok(response);
    }

    @Deprecated
    @PostMapping("/update-balance-with-salesperson")
    @Operation(
        summary = "[DEPRECATED] Add payment with salesperson — use POST /update-balance with salespersonId in body",
        deprecated = true)
    @ApiResponse(responseCode = "200", description = "Payment added for approval successfully")
    public ResponseEntity<PaymentApprovalResponse> updateBalanceWithSalesperson(
            @Valid @RequestBody PaymentSubmissionRequest request) {
        Long paymentId = paymentService.addPaymentFromRequest(request);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(paymentId);
        response.setMessage("Payment added for approval with salesperson");
        response.setStatus("PAYMENT_ADDED");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment-approval/{paymentId}")
    @Operation(summary = "Approve payment", description = "Approves payment and updates ledger. For CREDIT transactions, automatically restores creditBalance to creditLimit first, then adds remaining amount to ledger balance.")
    @ApiResponse(responseCode = "200", description = "Payment approved successfully")
    public ResponseEntity<String> paymentApproval(
            @PathVariable Long paymentId,
            @RequestParam Long approvedBy) {
        log.info("Entering paymentApproval() - paymentId: {}, approvedBy: {}", paymentId, approvedBy);
        paymentService.approvePayment(paymentId, approvedBy);
        log.info("Exiting paymentApproval() - approved paymentId: {}", paymentId);
        return ResponseEntity.ok("Payment approved and ledger updated");
    }

    @PostMapping("/payment-rejection/{paymentId}")
    @Operation(summary = "Reject payment", description = "Rejects payment added by distributor with reason")
    @ApiResponse(responseCode = "200", description = "Payment rejected successfully")
    public ResponseEntity<String> paymentRejection(
            @PathVariable Long paymentId,
            @RequestParam Long rejectedBy,
            @RequestBody PaymentRejectionRequest request) {
        log.info("Entering paymentRejection() - paymentId: {}, rejectedBy: {}", paymentId, rejectedBy);
        paymentService.rejectPayment(paymentId, rejectedBy, request.getReason());
        log.info("Exiting paymentRejection() - rejected paymentId: {}", paymentId);
        return ResponseEntity.ok("Payment rejected successfully");
    }

    @GetMapping("/pending-payments")
    @Operation(summary = "Get pending payments", description = "Retrieves payments with PAYMENT_ADDED status, optionally filtered by salesperson")
    @ApiResponse(responseCode = "200", description = "Pending payments retrieved successfully")
    public ResponseEntity<List<PaymentApproval>> getPendingPayments(
            @RequestParam(required = false) Long salespersonId) {
        List<PaymentApproval> pendingPayments = paymentService.getPendingPaymentsBySalesperson(salespersonId);
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/pending-payments-by-distributor")
    @Operation(summary = "Get pending payments by distributor", description = "Retrieves payments with PAYMENT_ADDED status, optionally filtered by distributor")
    @ApiResponse(responseCode = "200", description = "Pending payments retrieved successfully")
    public ResponseEntity<List<PaymentApproval>> getPendingPaymentsByDistributor(
            @RequestParam(required = false) Long distributorId) {
        List<PaymentApproval> pendingPayments = paymentService.getPendingPayments(distributorId);
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/all-pending-payments")
    @Operation(summary = "Get all pending payments", description = "Retrieves all payments with PAYMENT_ADDED status")
    @ApiResponse(responseCode = "200", description = "All pending payments retrieved successfully")
    public ResponseEntity<List<PaymentApproval>> getAllPendingPayments() {
        List<PaymentApproval> pendingPayments = paymentService.getAllPendingPayments();
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/payment-approved-orders")
    @Operation(summary = "Get all payment approved orders", description = "Retrieves all carts/orders with PAYMENT_APPROVED status")
    @ApiResponse(responseCode = "200", description = "Payment approved orders retrieved successfully")
    public ResponseEntity<List<Cart>> getPaymentApprovedOrders() {
        List<Cart> approvedOrders = cartRepository.findByStatus(Cart.CartStatus.PAYMENT_APPROVED);
        return ResponseEntity.ok(approvedOrders);
    }

    @GetMapping("/ledger-updated-payments")
    @Operation(summary = "Get ledger updated payments", description = "Retrieves payments with LEDGER_UPDATED status, optionally filtered by salespersonId and distributorId")
    @ApiResponse(responseCode = "200", description = "Ledger updated payments retrieved successfully")
    public ResponseEntity<List<PaymentApproval>> getLedgerUpdatedPayments(
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) Long distributorId) {
        List<PaymentApproval> ledgerUpdatedPayments = paymentService.getLedgerUpdatedPayments(salespersonId, distributorId);
        return ResponseEntity.ok(ledgerUpdatedPayments);
    }

    @GetMapping("/collection-analytics")
    @Operation(summary = "Get collection analytics", description = "Returns collectionMonthly (current month) and collectionYearly (all-time total) for LEDGER_UPDATED CREDIT payments by distributorId")
    @ApiResponse(responseCode = "200", description = "Collection analytics retrieved successfully")
    public ResponseEntity<Map<String, BigDecimal>> getCollectionAnalytics(
            @RequestParam Long distributorId) {
        return ResponseEntity.ok(paymentService.getCollectionAnalytics(distributorId));
    }

    private void populateDistributorNames(List<PaymentApproval> payments) {
        payments.forEach(payment -> {
            if (payment.getDistributorName() == null) {
                paymentService.getDistributorRepository().findById(payment.getDistributorId())
                        .ifPresent(distributor -> payment.setDistributorName(distributor.getFirstName()));
            }
        });
    }

    @GetMapping("/payments/{distributorId}")
    @Operation(summary = "Get payments by distributor ID", description = "Retrieves all payments for a distributor including all payment details")
    @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentsByDistributor(
            @PathVariable Long distributorId) {

        List<PaymentApproval> payments = paymentService.getPaymentsByDistributor(distributorId);
        
        // Populate distributor names
        populateDistributorNames(payments);

        List<PaymentStatusResponse> response = payments.stream()
                .map(payment -> {
                    PaymentStatusResponse dto = new PaymentStatusResponse();
                    dto.setPaymentId(payment.getId());
                    dto.setStatus(payment.getStatus());
                    dto.setDistributorId(payment.getDistributorId());
                    dto.setDistributorName(payment.getDistributorName());
                    dto.setAmount(payment.getAmount());
                    dto.setTransactionType(payment.getTransactionType());
                    dto.setDescription(payment.getDescription());
                    dto.setCreatedAt(payment.getCreatedAt());
                    dto.setApprovedAt(payment.getApprovedAt());
                    dto.setApprovedBy(payment.getApprovedBy());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/journal-voucher")
    @Operation(summary = "Process Journal Voucher", description = "Directly updates ledger with journal voucher entries")
    @ApiResponse(responseCode = "200", description = "Journal voucher processed successfully")
    public ResponseEntity<String> processJournalVoucher(
            @RequestParam Long distributorId,
            @RequestBody JournalVoucherRequest request) {
        paymentService.processJournalVoucher(distributorId, request);
        return ResponseEntity.ok("Journal voucher processed and ledger updated");
    }

    @GetMapping("/jv-by-distributor/{distributorId}")
    @Operation(summary = "Get Journal Vouchers by Distributor ID", description = "Retrieves all journal voucher entries for a specific distributor")
    @ApiResponse(responseCode = "200", description = "Journal vouchers retrieved successfully")
    public ResponseEntity<List<DistributorLedger>> getJournalVouchersByDistributor(@PathVariable Long distributorId) {
        List<DistributorLedger> jvEntries = paymentService.getJournalVouchersByDistributor(distributorId);
        return ResponseEntity.ok(jvEntries);
    }

    @PostMapping("/update-balance-accounts")
    @Operation(summary = "Add payment by accounts", description = "Directly updates distributor ledger for all transaction types")
    @ApiResponse(responseCode = "200", description = "Ledger updated successfully")
    public ResponseEntity<PaymentApprovalResponse> updateAccountsBalance(
            @RequestParam Long distributorId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType transactionType,
            @RequestParam String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {

        // CREDIT entries apply split: restore credit first, remainder to ledger.
        // DEBIT / JV entries write directly.
        paymentService.updateDistributorBalanceWithCreditSplit(distributorId, amount, transactionType.name(), description, date);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(null);
        response.setMessage(transactionType.name() + " transaction processed - ledger updated directly");
        response.setStatus("LEDGER_UPDATED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/credit-balance/{distributorId}")
    @Operation(summary = "Get credit balance", description = "Retrieves credit limit, credit balance, and usage information for a distributor")
    @ApiResponse(responseCode = "200", description = "Credit balance retrieved successfully")
    public ResponseEntity<CreditBalanceResponse> getCreditBalance(@PathVariable Long distributorId) {
        CreditBalanceResponse creditBalance = paymentService.getCreditBalance(distributorId);
        return ResponseEntity.ok(creditBalance);
    }

    @GetMapping("/payment-received-logs")
    @Operation(summary = "Get all payment received logs", description = "Returns all payment submissions across all parties with party name, payment mode (NEFT/RTGS/IMPS/UPI/CHEQUE/CASH), reference numbers, and the person who added each payment")
    @ApiResponse(responseCode = "200", description = "Payment received logs retrieved successfully")
    public ResponseEntity<List<PaymentReceivedLogResponse>> getPaymentReceivedLogs() {
        return ResponseEntity.ok(paymentService.getAllPaymentReceivedLogs());
    }

}
