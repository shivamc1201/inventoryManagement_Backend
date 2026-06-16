package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {

    private Long id;
    private String voucherNo;
    private LocalDate date;
    private LedgerType voucherType;
    private Long ledgerId;
    private String ledgerName;
    private String partyName;
    private String mobileNo;
    private String invoiceRef;
    private PaymentMode paymentMode;
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal lessAdjustment;
    private String narration;
    private VoucherStatus status;
    private LocalDate createdAt;
}
