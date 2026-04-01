package com.nector.userservice.dto.invoice;

import lombok.Data;

@Data
public class InvoiceItem {
    private int srNo;
    private String description;
    private String hsnCode;
    private String altQty;
    private int quantity;
    private double ratePerUnit;
    private String unit;
    private String per;
    private double amount;
}