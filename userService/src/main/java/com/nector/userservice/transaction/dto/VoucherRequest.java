package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VoucherRequest {

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "voucherType is required")
    private LedgerType voucherType;

    @NotNull(message = "ledgerId is required")
    private Long ledgerId;

    @NotBlank(message = "ledgerName is required")
    private String ledgerName;

    private String partyName;

    private String mobileNo;

    private String invoiceRef;

    @NotNull(message = "paymentMode is required")
    private PaymentMode paymentMode;

    private String transactionId;

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    private BigDecimal lessAdjustment = BigDecimal.ZERO;

    @NotBlank(message = "narration is required")
    private String narration;
}
