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
    
    @Schema(description = "Current closing balance", example = "15000.50")
    private BigDecimal closingBalance;
    
    @Schema(description = "Distributor ID", example = "123")
    private Long distributorId;
}
