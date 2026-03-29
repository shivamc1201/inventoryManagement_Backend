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
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

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

    @PostMapping("/update-balance-with-salesperson")
    @Operation(summary = "Add payment for approval with salesperson", description = "Creates payment entry with PAYMENT_ADDED status including salesperson ID")
    @ApiResponse(responseCode = "200", description = "Payment added for approval successfully")
    public ResponseEntity<PaymentApprovalResponse> updateBalanceWithSalesperson(
            @RequestParam Long distributorId,
            @RequestParam Long salespersonId,
            @RequestParam BigDecimal amount,
            @RequestParam TransactionType transactionType,
            @RequestParam String description) {
        Long paymentId = paymentService.addPaymentForApprovalWithSalesperson(distributorId, salespersonId, amount, transactionType.name(), description);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(paymentId);
        response.setMessage("Payment added for approval with salesperson");
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

    @PostMapping("/payment-rejection/{paymentId}")
    @Operation(summary = "Reject payment", description = "Rejects payment added by distributor with reason")
    @ApiResponse(responseCode = "200", description = "Payment rejected successfully")
    public ResponseEntity<String> paymentRejection(
            @PathVariable Long paymentId,
            @RequestParam Long rejectedBy,
            @RequestBody PaymentRejectionRequest request) {
        paymentService.rejectPayment(paymentId, rejectedBy, request.getReason());
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
            @RequestParam String description) {

        // Directly update ledger for all transaction types (JV, CREDIT, DEBIT)
        paymentService.updateDistributorBalance(distributorId, amount, transactionType.name(), description);

        PaymentApprovalResponse response = new PaymentApprovalResponse();
        response.setPaymentId(null);
        response.setMessage(transactionType.name() + " transaction processed - ledger updated directly");
        response.setStatus("LEDGER_UPDATED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-history/{distributorId}/download")
    @Operation(summary = "Download payment history as CSV", description = "Downloads payment history for a distributor in CSV format")
    @ApiResponse(responseCode = "200", description = "Payment history CSV downloaded successfully")
    public ResponseEntity<byte[]> downloadPaymentHistory(@PathVariable Long distributorId) {
        List<DistributorLedger> history = paymentService.getPaymentHistory(distributorId);
        
        // Get distributor name for the header
        String distributorName = paymentService.getDistributorRepository()
                .findById(distributorId)
                .map(distributor -> distributor.getFirstName() + " " + distributor.getLastName())
                .orElse("Unknown Distributor");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        // Write header
        writer.println("Nectar Origin Private Limited");
        writer.println("360 K, Shiv Parwati Nagar, Block Road No-2,");
        writer.println("Ward No 16, Kahalgaon, Bhagalpur Bihar");
        writer.println("Bihar - 813203, India");
        writer.println("CIN: U74999BR2016PTC032690");
        writer.println("Contact : 06429-450126,9797979522");
        writer.println("E-Mail : nectarorigin@gmail.com");
        writer.println("http://www.nectarorigin.com");
        writer.println();
        writer.println("LEDGER ACCOUNT: " + distributorName.toUpperCase());
        writer.println("FROM: Nectar Origin Private Limited");
        writer.println("PERIOD: 01-04-2025 TO " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        writer.println();
        
        // Write CSV headers
        writer.println("Date,Particulars,Vch Type,Vch No.,Debit,Credit");
        
        // Write data rows
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (DistributorLedger entry : history) {
            String date = entry.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String particulars = entry.getDescription();
            String vchType = entry.getTransactionType();
            String vchNo = entry.getId().toString();
            String debit = "";
            String credit = "";
            
            if ("DEBIT".equalsIgnoreCase(entry.getTransactionType())) {
                particulars = "Dr " + particulars;
                debit = String.format("%,.2f", entry.getAmount());
                totalDebit = totalDebit.add(entry.getAmount());
            } else {
                particulars = "Cr " + particulars;
                credit = String.format("%,.2f", entry.getAmount());
                totalCredit = totalCredit.add(entry.getAmount());
            }
            
            writer.println(String.format("%s,%s,%s,%s,%s,%s", 
                    date, particulars, vchType, vchNo, debit, credit));
        }
        
        // Add total row
        writer.println(String.format("TOTAL,,,,%s,%s", 
                String.format("%,.2f", totalDebit), String.format("%,.2f", totalCredit)));
        
        writer.flush();
        byte[] csvBytes = outputStream.toByteArray();
        writer.close();
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"ledger_" + distributorId + ".csv\"")
                .body(csvBytes);
    }

    @GetMapping("/payment-history/{distributorId}/download-pdf")
    @Operation(summary = "Download payment history as PDF", description = "Downloads payment history for a distributor in PDF format")
    @ApiResponse(responseCode = "200", description = "Payment history PDF downloaded successfully")
    public ResponseEntity<byte[]> downloadPaymentHistoryPDF(@PathVariable Long distributorId) throws DocumentException {
        List<DistributorLedger> history = paymentService.getPaymentHistory(distributorId);
        
        // Get distributor name for the header
        String distributorName = paymentService.getDistributorRepository()
                .findById(distributorId)
                .map(distributor -> distributor.getFirstName() + " " + distributor.getLastName())
                .orElse("Unknown Distributor");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Add company header
        Font companyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        document.add(new Paragraph("Nectar Origin Private Limited", new Font(Font.HELVETICA, 12, Font.BOLD)));
        document.add(new Paragraph("360 K, Shiv Parwati Nagar, Block Road No-2,", companyFont));
        document.add(new Paragraph("Ward No 16, Kahalgaon, Bhagalpur Bihar", companyFont));
        document.add(new Paragraph("Bihar - 813203, India", companyFont));
        document.add(new Paragraph("CIN: U74999BR2016PTC032690", companyFont));
        document.add(new Paragraph("Contact : 06429-450126,9797979522", companyFont));
        document.add(new Paragraph("E-Mail : nectarorigin@gmail.com", companyFont));
        document.add(new Paragraph("http://www.nectarorigin.com", companyFont));
        document.add(new Paragraph(" "));
        
        // Add title and header
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font tableFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
        
        Paragraph title = new Paragraph("LEDGER ACCOUNT: " + distributorName.toUpperCase(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph company = new Paragraph("FROM: Nectar Origin Private Limited", headerFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);
        
        Paragraph period = new Paragraph("PERIOD: 01-04-2025 TO " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), headerFont);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        
        document.add(Chunk.NEWLINE);
        
        // Create table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 4f, 1.5f, 1.5f, 2f, 2f});
        
        // Table headers
        table.addCell(new PdfPCell(new Phrase("Date", tableHeaderFont)));
        table.addCell(new PdfPCell(new Phrase("Particulars", tableHeaderFont)));
        table.addCell(new PdfPCell(new Phrase("Vch Type", tableHeaderFont)));
        table.addCell(new PdfPCell(new Phrase("Vch No.", tableHeaderFont)));
        table.addCell(new PdfPCell(new Phrase("Debit", tableHeaderFont)));
        table.addCell(new PdfPCell(new Phrase("Credit", tableHeaderFont)));
        
        // Table data
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (DistributorLedger entry : history) {
            String date = entry.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String particulars = entry.getDescription();
            String vchType = entry.getTransactionType();
            String vchNo = entry.getId().toString();
            String debit = "";
            String credit = "";
            
            if ("DEBIT".equalsIgnoreCase(entry.getTransactionType())) {
                particulars = "Dr " + particulars;
                debit = String.format("%,.2f", entry.getAmount());
                totalDebit = totalDebit.add(entry.getAmount());
            } else {
                particulars = "Cr " + particulars;
                credit = String.format("%,.2f", entry.getAmount());
                totalCredit = totalCredit.add(entry.getAmount());
            }
            
            table.addCell(new PdfPCell(new Phrase(date, tableFont)));
            table.addCell(new PdfPCell(new Phrase(particulars, tableFont)));
            table.addCell(new PdfPCell(new Phrase(vchType, tableFont)));
            table.addCell(new PdfPCell(new Phrase(vchNo, tableFont)));
            
            PdfPCell debitCell = new PdfPCell(new Phrase(debit, tableFont));
            debitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(debitCell);
            
            PdfPCell creditCell = new PdfPCell(new Phrase(credit, tableFont));
            creditCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(creditCell);
        }
        
        // Add total row
        PdfPCell totalTextCell = new PdfPCell(new Phrase("TOTAL", new Font(Font.HELVETICA, 10, Font.BOLD)));
        totalTextCell.setColspan(4);
        totalTextCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        table.addCell(totalTextCell);
        
        PdfPCell totalDebitCell = new PdfPCell(new Phrase(String.format("%,.2f", totalDebit), new Font(Font.HELVETICA, 10, Font.BOLD)));
        totalDebitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalDebitCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        table.addCell(totalDebitCell);
        
        PdfPCell totalCreditCell = new PdfPCell(new Phrase(String.format("%,.2f", totalCredit), new Font(Font.HELVETICA, 10, Font.BOLD)));
        totalCreditCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCreditCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        table.addCell(totalCreditCell);
        
        document.add(table);
        document.close();
        
        byte[] pdfBytes = outputStream.toByteArray();
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"ledger_" + distributorId + ".pdf\"")
                .body(pdfBytes);
    }
}
