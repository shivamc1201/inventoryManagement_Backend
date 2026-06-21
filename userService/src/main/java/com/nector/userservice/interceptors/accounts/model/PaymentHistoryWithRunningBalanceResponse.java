package com.nector.userservice.interceptors.accounts.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Payment history response with running balance for each transaction")
public class PaymentHistoryWithRunningBalanceResponse {
    
    @Schema(description = "Current closing balance", example = "10076")
    private BigDecimal closingBalance;
    
    @Schema(description = "Distributor ID", example = "1")
    private Long distributorId;
    
    @Schema(description = "List of payment transactions with running balance")
    private List<PaymentHistoryWithBalance> paymentHistory;
    
    @Data
    @Schema(description = "Payment transaction with running balance")
    public static class PaymentHistoryWithBalance {
        
        @Schema(description = "Transaction ID", example = "20")
        private Long id;
        
        @Schema(description = "Transaction amount", example = "18800")
        private BigDecimal amount;
        
        @Schema(description = "Transaction date", example = "2026-03-22T22:42:30.104106")
        private LocalDateTime createdAt;
        
        @Schema(description = "Transaction description", example = "teswst")
        private String description;
        
        @Schema(description = "Distributor ID", example = "1")
        private Long distributorId;
        
        @Schema(description = "Transaction type", example = "DEBIT")
        private String transactionType;
        
        @Schema(description = "Running balance after this transaction", example = "18800")
        private BigDecimal currentCB;
    }
}
