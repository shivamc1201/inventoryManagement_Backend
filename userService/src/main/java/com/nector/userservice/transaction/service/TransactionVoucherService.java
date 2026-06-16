package com.nector.userservice.transaction.service;

import com.nector.userservice.exception.LedgerNotFoundException;
import com.nector.userservice.exception.TransactionValidationException;
import com.nector.userservice.exception.VoucherNotFoundException;
import com.nector.userservice.transaction.dto.VoucherRequest;
import com.nector.userservice.transaction.dto.VoucherResponse;
import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.entity.TransactionVoucher;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.enums.VoucherStatus;
import com.nector.userservice.transaction.repository.TransactionLedgerRepository;
import com.nector.userservice.transaction.repository.TransactionVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionVoucherService {

    private final TransactionVoucherRepository voucherRepository;
    private final TransactionLedgerRepository ledgerRepository;
    private final TransactionNumberService numberService;
    private final TransactionFundService fundService;

    public VoucherResponse createVoucher(VoucherRequest request) {
        TransactionLedger ledger = ledgerRepository.findById(request.getLedgerId())
                .orElseThrow(() -> new LedgerNotFoundException("Ledger with id " + request.getLedgerId() + " not found"));

        if (ledger.getLedgerType() != request.getVoucherType()) {
            throw new TransactionValidationException(
                    "ledger '" + ledger.getLedgerName() + "' has ledgerType '" + ledger.getLedgerType()
                            + "' which does not match voucherType '" + request.getVoucherType() + "'");
        }

        if (request.getPaymentMode() == PaymentMode.UPI
                && (request.getTransactionId() == null || request.getTransactionId().isBlank())) {
            throw new TransactionValidationException("transactionId is required when paymentMode is UPI");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException("amount must be greater than 0");
        }

        BigDecimal lessAdjustment = request.getLessAdjustment() == null ? BigDecimal.ZERO : request.getLessAdjustment();
        if (lessAdjustment.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransactionValidationException("lessAdjustment must not be negative");
        }
        if (lessAdjustment.compareTo(amount) > 0) {
            throw new TransactionValidationException("lessAdjustment must not exceed amount");
        }

        if (request.getNarration() == null || request.getNarration().isBlank()) {
            throw new TransactionValidationException("narration is required");
        }

        TransactionVoucher voucher = new TransactionVoucher();
        voucher.setVoucherNo(numberService.nextVoucherNo());
        voucher.setDate(request.getDate());
        voucher.setVoucherType(request.getVoucherType());
        voucher.setLedgerId(request.getLedgerId());
        voucher.setLedgerName(request.getLedgerName());
        voucher.setPartyName(request.getPartyName());
        voucher.setMobileNo(request.getMobileNo());
        voucher.setInvoiceRef(request.getInvoiceRef());
        voucher.setPaymentMode(request.getPaymentMode());
        voucher.setTransactionId(request.getTransactionId());
        voucher.setAmount(amount);
        voucher.setLessAdjustment(lessAdjustment);
        voucher.setNarration(request.getNarration());
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setCreatedAt(LocalDate.now());

        TransactionVoucher saved = voucherRepository.save(voucher);
        return toResponse(saved);
    }

    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<VoucherResponse> getVouchersByLedgerName(String ledgerName) {
        if (TransactionLedgerService.RECEIVED_FROM_HO.equals(ledgerName)) {
            return fundService.getFundsAsVouchers();
        }
        return voucherRepository.findByLedgerName(ledgerName).stream().map(this::toResponse).toList();
    }

    public VoucherResponse getVoucherByNo(String voucherNo) {
        String normalized = voucherNo.trim().toUpperCase();
        return voucherRepository.findByVoucherNo(normalized)
                .map(this::toResponse)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher '" + normalized + "' not found"));
    }

    private VoucherResponse toResponse(TransactionVoucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .date(voucher.getDate())
                .voucherType(voucher.getVoucherType())
                .ledgerId(voucher.getLedgerId())
                .ledgerName(voucher.getLedgerName())
                .partyName(voucher.getPartyName())
                .mobileNo(voucher.getMobileNo())
                .invoiceRef(voucher.getInvoiceRef())
                .paymentMode(voucher.getPaymentMode())
                .transactionId(voucher.getTransactionId())
                .amount(voucher.getAmount())
                .lessAdjustment(voucher.getLessAdjustment())
                .narration(voucher.getNarration())
                .status(voucher.getStatus())
                .createdAt(voucher.getCreatedAt())
                .build();
    }
}
