package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.FundLocation;
import com.nector.userservice.transaction.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundResponse {

    private Long id;
    private String fundNo;
    private LocalDate date;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private String transactionId;
    private FundLocation location;
    private String narration;
    private LocalDate createdAt;
}
