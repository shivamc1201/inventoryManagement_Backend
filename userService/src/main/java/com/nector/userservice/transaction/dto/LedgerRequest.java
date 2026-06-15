package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LedgerRequest {

    @NotBlank(message = "ledgerName is required")
    private String ledgerName;

    @NotNull(message = "ledgerType is required")
    private LedgerType ledgerType;

    @NotNull(message = "underGroup is required")
    private UnderGroup underGroup;
}
