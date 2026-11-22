package com.nector.userservice.dto.payment;

import com.nector.userservice.enums.PaymentMode;
import com.nector.userservice.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentSubmissionRequest {

    @NotNull(message = "distributorId is required")
    private Long distributorId;

    @NotNull(message = "salespersonId is required")
    private Long salespersonId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "transactionType is required (CREDIT or DEBIT)")
    private TransactionType transactionType;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @NotNull(message = "paymentMode is required (RTGS, NEFT, IMPS, UPI, CHEQUE, CASH)")
    private PaymentMode paymentMode;

    // RTGS / NEFT
    private String utrNumber;

    // CHEQUE
    private String chequeNumber;
    private String bankName;

    // IMPS / UPI
    private String transactionNumber;

    @AssertTrue(message = "utrNumber is required for RTGS and NEFT payments")
    private boolean isUtrNumberValidForMode() {
        if (paymentMode == PaymentMode.RTGS || paymentMode == PaymentMode.NEFT) {
            return utrNumber != null && !utrNumber.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "chequeNumber is required for CHEQUE payments")
    private boolean isChequeNumberValidForMode() {
        if (paymentMode == PaymentMode.CHEQUE) {
            return chequeNumber != null && !chequeNumber.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "bankName is required for CHEQUE payments")
    private boolean isBankNameValidForMode() {
        if (paymentMode == PaymentMode.CHEQUE) {
            return bankName != null && !bankName.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "transactionNumber is required for IMPS and UPI payments")
    private boolean isTransactionNumberValidForMode() {
        if (paymentMode == PaymentMode.IMPS || paymentMode == PaymentMode.UPI) {
            return transactionNumber != null && !transactionNumber.isBlank();
        }
        return true;
    }
}
