package com.nector.userservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentReceivedLogResponse {
    private Long paymentId;
    private Long distributorId;
    private String partyName;
    private BigDecimal amount;
    private String transactionType;
    private String paymentMode;        // NEFT / RTGS / IMPS / UPI / CHEQUE / CASH
    private String utrNumber;          // RTGS / NEFT
    private String chequeNumber;       // CHEQUE
    private String bankName;           // CHEQUE
    private String transactionNumber;  // IMPS / UPI
    private String description;
    private String status;
    private String addedByName;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
