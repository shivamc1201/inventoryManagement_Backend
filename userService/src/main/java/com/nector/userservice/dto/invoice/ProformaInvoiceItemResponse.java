package com.nector.userservice.dto.invoice;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProformaInvoiceItemResponse {
    private Long id;
    private Integer srNo;
    private String description;
    private String hsnCode;
    private Integer quantity;
    private String unit;
    private BigDecimal ratePerUnit;
    private BigDecimal amount;
}
