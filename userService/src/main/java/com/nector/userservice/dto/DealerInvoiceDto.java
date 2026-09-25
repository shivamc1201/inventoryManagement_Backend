package com.nector.userservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DealerInvoiceDto {

    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String orderNo;
    private String orderDate;
    private String paymentTerms;

    // Seller = Distributor
    private String sellerName;
    private String sellerAddress;
    private String sellerPhone;
    private String sellerEmail;
    private String sellerGstin;
    private String sellerState;
    private String sellerPan;
    private String sellerBankAccountHolder;
    private String sellerBankName;
    private String sellerBankAccountNo;
    private String sellerBankIfsc;
    private String taxInWords;

    // Buyer = Dealer
    private String buyerName;
    private String buyerAddress;
    private String buyerPhone;

    // Items
    private List<Item> items;

    // Totals
    private double subtotal;
    private double grandTotal;
    private String totalQty;
    private String amountInWords;

    @Data
    public static class Item {
        private int srNo;
        private String sku;
        private String description;
        private int quantity;
        private double ratePerUnit;
        private double amount;
    }
}
