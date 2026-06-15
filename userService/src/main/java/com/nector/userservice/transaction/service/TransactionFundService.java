package com.nector.userservice.transaction.service;

import com.nector.userservice.exception.TransactionValidationException;
import com.nector.userservice.transaction.dto.FundRequest;
import com.nector.userservice.transaction.dto.FundResponse;
import com.nector.userservice.transaction.dto.VoucherResponse;
import com.nector.userservice.transaction.entity.TransactionFund;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.enums.VoucherStatus;
import com.nector.userservice.transaction.repository.TransactionFundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionFundService {

    private final TransactionFundRepository fundRepository;
    private final TransactionNumberService numberService;

    public FundResponse createFund(FundRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException("amount must be greater than 0");
        }
        if (request.getPaymentMode() == PaymentMode.UPI
                && (request.getTransactionId() == null || request.getTransactionId().isBlank())) {
            throw new TransactionValidationException("transactionId is required when paymentMode is UPI");
        }

        TransactionFund fund = new TransactionFund();
        fund.setFundNo(numberService.nextFundNo());
        fund.setDate(request.getDate());
        fund.setAmount(request.getAmount());
        fund.setPaymentMode(request.getPaymentMode());
        fund.setTransactionId(request.getTransactionId());
        fund.setLocation(request.getLocation());
        fund.setNarration(request.getNarration());
        fund.setCreatedAt(LocalDate.now());

        TransactionFund saved = fundRepository.save(fund);
        return toResponse(saved);
    }

    public List<FundResponse> getAllFunds() {
        return fundRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<VoucherResponse> getFundsAsVouchers() {
        return fundRepository.findAll().stream().map(this::toVoucherResponse).toList();
    }

    private FundResponse toResponse(TransactionFund fund) {
        return new FundResponse(
                fund.getId(),
                fund.getFundNo(),
                fund.getDate(),
                fund.getAmount(),
                fund.getPaymentMode(),
                fund.getTransactionId(),
                fund.getLocation(),
                fund.getNarration(),
                fund.getCreatedAt());
    }

    private VoucherResponse toVoucherResponse(TransactionFund fund) {
        return VoucherResponse.builder()
                .id(fund.getId())
                .voucherNo(fund.getFundNo())
                .date(fund.getDate())
                .voucherType(LedgerType.INCOME)
                .ledgerId(null)
                .ledgerName(TransactionLedgerService.RECEIVED_FROM_HO)
                .partyName("Head Office")
                .mobileNo("")
                .invoiceRef("")
                .paymentMode(fund.getPaymentMode())
                .transactionId(fund.getTransactionId())
                .amount(fund.getAmount())
                .lessAdjustment(BigDecimal.ZERO)
                .narration(fund.getNarration())
                .status(VoucherStatus.APPROVED)
                .createdAt(fund.getCreatedAt())
                .build();
    }
}
