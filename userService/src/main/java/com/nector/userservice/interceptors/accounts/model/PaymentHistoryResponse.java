package com.nector.userservice.interceptors.accounts.model;

import com.nector.userservice.model.DistributorLedger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Payment history response with closing balance")
public class PaymentHistoryResponse {
    
    @Schema(description = "List of payment transactions")
    private List<DistributorLedger> paymentHistory;
    
    @Schema(description = "Closing balance (total credits minus total debits across all transactions)", example = "15000.50")
    private BigDecimal closingBalance;

    @Schema(description = "Net ledger balance (excludes credit-line entries)", example = "13000.00")
    private BigDecimal netBalance;

    @Schema(description = "Sum of all credit entries", example = "27000.00")
    private BigDecimal totalCredits;

    @Schema(description = "Sum of all debit entries", example = "12000.00")
    private BigDecimal totalDebits;

    @Schema(description = "Distributor ID", example = "123")
    private Long distributorId;
}
