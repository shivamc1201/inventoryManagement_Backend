package com.nector.userservice.interceptors.accounts;

import com.nector.userservice.dto.payment.*;
import com.nector.userservice.enums.TransactionType;
import com.nector.userservice.model.PaymentApproval;
import com.nector.userservice.model.ProformaInvoice;
import com.nector.userservice.model.DistributorLedger;
import com.nector.userservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "APIs for Accounts Team management")
public class AccountsController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/process-payment")
    @Operation(summary = "Process payment", description = "Processes payment and updates distributor ledger")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse processedPayment = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(processedPayment);
    }
    
    @PostMapping("/approve-PI/{orderId}")
    @Operation(summary = "Check and approve PI", description = "Checks distributor balance and approves PI if sufficient funds")
    @ApiResponse(responseCode = "200", description = "Order approval status returned")
    public ResponseEntity<OrderApprovalResponse> approveOrder(
            @PathVariable Long orderId,
            @RequestParam Long distributorId,
            @RequestParam Long salespersonId) {
        
        // Check if cart is active
        if (!paymentService.isCartApproved(orderId)) {
            OrderApprovalResponse response = new OrderApprovalResponse();
            response.setOrderId(orderId);
            response.setDistributorId(distributorId);
            response.setStatus("INVALID_CART");
            response.setMessage("Cart is not approved or does not exist");
            return ResponseEntity.status(400).body(response);
        }
        
        // Check if salesperson is authorized for this distributor
        if (!paymentService.isSalespersonAuthorizedForDistributor(salespersonId, distributorId)) {
            OrderApprovalResponse response = new OrderApprovalResponse();
            response.setOrderId(orderId);
            response.setDistributorId(distributorId);
            response.setStatus("UNAUTHORIZED");
            response.setMessage("Salesperson not authorized to approve orders for this distributor");
            return ResponseEntity.status(403).body(response);
        }
        
        OrderApprovalResponse approval = paymentService.checkAndApproveOrder(orderId, distributorId);
        return ResponseEntity.ok(approval);
    }
    
    @GetMapping("/pending-pi-payments")
    @Operation(summary = "Get all pending PI payments", description = "Retrieves all proforma invoices with pending payment status")
    @ApiResponse(responseCode = "200", description = "Pending PI payments retrieved successfully")
    public ResponseEntity<List<ProformaInvoice>> getPendingPIPayments() {
        List<ProformaInvoice> pendingPIs = paymentService.getPendingPIPayments();
        return ResponseEntity.ok(pendingPIs);
    }
    
    @PostMapping("/approve-payment/{orderId}")
    @Operation(summary = "Approve payment for order", description = "Approves payment and updates order status to PAYMENT_APPROVED")
    @ApiResponse(responseCode = "200", description = "Payment approved successfully")
    public ResponseEntity<OrderApprovalResponse> approvePayment(
            @PathVariable Long orderId,
            @RequestParam Long distributorId) {
        OrderApprovalResponse approval = paymentService.approvePaymentForOrder(orderId, distributorId);
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
    @Operation(summary = "Get payment history", description = "Retrieves payment history for a distributor")
    @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully")
    public ResponseEntity<List<DistributorLedger>> getPaymentHistory(@PathVariable Long distributorId) {
        List<DistributorLedger> history = paymentService.getPaymentHistory(distributorId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/update-balance")
    @Operation(summary = "Add payment for approval", description = "Creates payment entry with PAYMENT_ADDED status")
    @ApiResponse(responseCode = "200", description = "Payment added for approval successfully")
    public ResponseEntity<PaymentApprovalResponse> updateBalance(
            @RequestParam Long distributorId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType transactionType,
            @RequestParam String description) {
        Long paymentId = paymentService.addPaymentForApproval(distributorId, amount, transactionType.name(), description);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(paymentId);
        response.setMessage("Payment added for approval");
        response.setStatus("PAYMENT_ADDED");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment-approval/{paymentId}")
    @Operation(summary = "Approve payment", description = "Approves payment added distributor ")
    @ApiResponse(responseCode = "200", description = "Payment approved successfully")
    public ResponseEntity<String> paymentApproval(
            @PathVariable Long paymentId,
            @RequestParam Long approvedBy) {
        paymentService.approvePayment(paymentId, approvedBy);
        return ResponseEntity.ok("Payment approved and ledger updated");
    }

    @GetMapping("/pending-payments")
    @Operation(summary = "Get pending payments", description = "Retrieves payments with PAYMENT_ADDED status, optionally filtered by distributor")
    @ApiResponse(responseCode = "200", description = "Pending payments retrieved successfully")
    public ResponseEntity<List<PaymentApproval>> getPendingPayments(
            @RequestParam(required = false) Long distributorId) {
        List<PaymentApproval> pendingPayments = paymentService.getPendingPayments(distributorId);
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/payments/{distributorId}")
    @Operation(summary = "Get payments by distributor ID and status", description = "Retrieves payments for a distributor with PAYMENT_ADDED status")
    @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentsByDistributorAndStatus(
            @PathVariable Long distributorId,
            @RequestParam(defaultValue = "PAYMENT_ADDED") String status) {

        List<PaymentApproval> payments = paymentService.getPaymentsByDistributorAndStatus(distributorId, status);

        List<PaymentStatusResponse> response = payments.stream()
                .map(payment -> {
                    PaymentStatusResponse dto = new PaymentStatusResponse();
                    dto.setPaymentId(payment.getId());
                    dto.setStatus(payment.getStatus());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/journal-voucher")
    @Operation(summary = "Process Journal Voucher", description = "Directly updates ledger with journal voucher entries")
    @ApiResponse(responseCode = "200", description = "Journal voucher processed successfully")
    public ResponseEntity<String> processJournalVoucher(@RequestBody JournalVoucherRequest request) {
        paymentService.processJournalVoucher(request);
        return ResponseEntity.ok("Journal voucher processed and ledger updated");
    }

    @PostMapping("/update-balance-accounts")
    @Operation(summary = "Add payment by accounts", description = "Creates payment entry with PAYMENT_ADDED status")
    @ApiResponse(responseCode = "200", description = "Payment added for approval successfully")
    public ResponseEntity<PaymentApprovalResponse> updateAccountsBalance(
            @RequestParam Long distributorId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType transactionType,
            @RequestParam String description) {

        if (transactionType == TransactionType.JV) {
            // For JV transactions, update ledger directly
            paymentService.updateDistributorBalance(distributorId, amount, "JV", description);

            PaymentApprovalResponse response = new PaymentApprovalResponse();
            response.setPaymentId(null);
            response.setMessage("Journal voucher processed - ledger updated directly");
            response.setStatus("LEDGER_UPDATED");

            return ResponseEntity.ok(response);
        } else {
            // For other transaction types, use existing approval process
            Long paymentId = paymentService.addPaymentForApproval(distributorId, amount, transactionType.name(), description);

            PaymentApprovalResponse response = new PaymentApprovalResponse();
            response.setPaymentId(paymentId);
            response.setMessage("Payment added for approval");
            response.setStatus("PAYMENT_ADDED");

            return ResponseEntity.ok(response);
        }
    }
}
