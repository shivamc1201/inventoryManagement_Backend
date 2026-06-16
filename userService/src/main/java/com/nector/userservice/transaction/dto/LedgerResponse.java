package com.nector.userservice.transaction.dto;

import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerResponse {

    private Long id;
    private String ledgerName;
    private LedgerType ledgerType;
    private UnderGroup underGroup;
    private LocalDate createdAt;
}
