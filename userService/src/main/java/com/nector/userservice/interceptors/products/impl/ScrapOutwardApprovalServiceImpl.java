package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.interceptors.products.service.ScrapOutwardApprovalService;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.model.ScrapOutwardApproval;
import com.nector.userservice.repository.ScrapOutwardApprovalRepository;
import com.nector.userservice.transaction.dto.VoucherRequest;
import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.repository.TransactionLedgerRepository;
import com.nector.userservice.transaction.service.TransactionVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapOutwardApprovalServiceImpl implements ScrapOutwardApprovalService {

    private static final String SALE_FROM_SCRAP_LEDGER = "Sale From Scrap";

    private final ScrapOutwardApprovalRepository approvalRepository;
    private final TransactionLedgerRepository ledgerRepository;
    private final TransactionVoucherService voucherService;

    @Override
    @Transactional
    public ScrapOutwardApproval createApproval(OutwardItemTransaction transaction) {
        ScrapOutwardApproval approval = new ScrapOutwardApproval();
        approval.setOutwardTransactionId(transaction.getId());
        approval.setMaterialCode(transaction.getMaterialCode());
        approval.setMaterialName(transaction.getMaterialName());
        approval.setQuantity(transaction.getQuantity());
        approval.setQuotedSellingPrice(transaction.getQuotedSellingPrice());
        approval.setIssuedTo(transaction.getIssuedTo());
        approval.setApprovalStatus("PENDING");

        ScrapOutwardApproval saved = approvalRepository.save(approval);
        log.info("Created scrap outward approval {} for outward transaction {}", saved.getId(), transaction.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapOutwardApproval> getPendingApprovals() {
        return approvalRepository.findByApprovalStatus("PENDING");
    }

    @Override
    @Transactional
    public Map<String, Object> processApproval(Long approvalId, String action, String adminUsername, String comments) {
        ScrapOutwardApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Scrap outward approval not found: " + approvalId));

        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new RuntimeException("Approval is already " + approval.getApprovalStatus());
        }

        Map<String, Object> response = new HashMap<>();

        if ("APPROVE".equals(action)) {
            createCashbookEntry(approval);
            approval.setApprovalStatus("APPROVED");
            response.put("message", "Scrap outward approved and cashbook entry created under 'Sale From Scrap'.");
        } else if ("REJECT".equals(action)) {
            approval.setApprovalStatus("REJECTED");
            response.put("message", "Scrap outward request rejected.");
        } else {
            throw new RuntimeException("Invalid action: " + action + ". Must be APPROVE or REJECT.");
        }

        approval.setReviewedBy(adminUsername);
        approval.setReviewedOn(LocalDateTime.now());
        approval.setReviewComments(comments);
        approvalRepository.save(approval);

        response.put("approvalId", approvalId);
        response.put("action", action);
        return response;
    }

    private void createCashbookEntry(ScrapOutwardApproval approval) {
        TransactionLedger ledger = ledgerRepository.findByLedgerNameIgnoreCase(SALE_FROM_SCRAP_LEDGER)
                .orElseThrow(() -> new RuntimeException("Ledger '" + SALE_FROM_SCRAP_LEDGER + "' not found in transaction_ledger table"));

        BigDecimal price = approval.getQuotedSellingPrice() != null ? approval.getQuotedSellingPrice() : BigDecimal.ZERO;
        BigDecimal amount = price.multiply(BigDecimal.valueOf(approval.getQuantity()));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Scrap outward approval {} has zero or negative amount, skipping cashbook entry", approval.getId());
            return;
        }

        VoucherRequest voucher = new VoucherRequest();
        voucher.setDate(LocalDate.now());
        voucher.setVoucherType(LedgerType.INCOME);
        voucher.setLedgerId(ledger.getId());
        voucher.setLedgerName(ledger.getLedgerName());
        voucher.setPartyName(approval.getIssuedTo());
        voucher.setAmount(amount);
        voucher.setLessAdjustment(BigDecimal.ZERO);
        voucher.setPaymentMode(PaymentMode.CASH);
        voucher.setNarration("Sale of scrap: " + approval.getMaterialName()
                + " (" + approval.getMaterialCode() + "), Qty: " + approval.getQuantity()
                + " @ Rs." + price);

        voucherService.createVoucher(voucher);
        log.info("Cashbook entry created under '{}' for scrap outward approval {}, amount: {}",
                SALE_FROM_SCRAP_LEDGER, approval.getId(), amount);
    }
}
