package com.nector.userservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LedgerPdfRowDto {
    private String formattedDate;
    private String description;
    private String reference;
    private String type;
    private String category;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
    // Pre-formatted display strings (used in PDF template)
    private String debitDisplay;
    private String creditDisplay;
    private String balanceDisplay;
}

