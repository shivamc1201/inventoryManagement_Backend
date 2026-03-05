package com.nector.userservice.interceptors.accounts;

import com.nector.userservice.dto.payment.PaymentRequest;
import com.nector.userservice.dto.payment.PaymentResponse;
import com.nector.userservice.dto.payment.OrderApprovalResponse;
import com.nector.userservice.enums.TransactionType;
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
            @RequestParam BigDecimal orderAmount,
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
        
        OrderApprovalResponse approval = paymentService.checkAndApproveOrder(orderId, distributorId, orderAmount);
        return ResponseEntity.ok(approval);
    }
    
    @GetMapping("/pending-pi-payments")
    @Operation(summary = "Get all pending PI payments", description = "Retrieves all proforma invoices with pending payment status")
    @ApiResponse(responseCode = "200", description = "Pending PI payments retrieved successfully")
    public ResponseEntity<List<ProformaInvoice>> getPendingPIPayments() {
        List<ProformaInvoice> pendingPIs = paymentService.getPendingPIPayments();
        return ResponseEntity.ok(pendingPIs);
    }
    
    @PostMapping("/update-balance")
    @Operation(summary = "Update distributor balance", description = "Updates distributor ledger with credit/debit transaction")
    @ApiResponse(responseCode = "200", description = "Balance updated successfully")
    public ResponseEntity<String> updateBalance(
            @RequestParam Long distributorId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType transactionType,
            @RequestParam String description) {
        paymentService.updateDistributorBalance(distributorId, amount, transactionType.name(), description);
        return ResponseEntity.ok("Balance updated successfully");
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
}
