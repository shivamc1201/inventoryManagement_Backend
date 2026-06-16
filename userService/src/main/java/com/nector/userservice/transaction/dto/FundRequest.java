package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.FundLocation;
import com.nector.userservice.transaction.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FundRequest {

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    @NotNull(message = "paymentMode is required")
    private PaymentMode paymentMode;

    private String transactionId;

    @NotNull(message = "location is required")
    private FundLocation location;

    private String narration;
}
