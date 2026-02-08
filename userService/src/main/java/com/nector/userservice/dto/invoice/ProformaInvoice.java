package com.nector.userservice.dto.invoice;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProformaInvoice {
    private String piNumber;
    private LocalDate piDate;
    private String modeOfPayment;
    
    // Seller details
    private String companyName;
    private String companyAddress;
    private String gstin;
    private String contactNumber;
    private String email;
    
    // Buyer details
    private BuyerDetails billTo;
    private BuyerDetails shipTo;
    
    // Items
    private List<InvoiceItem> items;
    
    // Tax details
    private double subtotal;
    private double cgst;
    private double sgst;
    private double igst;
    private double grandTotal;
    private String amountInWords;
    
    @Data
    public static class BuyerDetails {
        private String name;
        private String address;
        private String state;
        private String stateCode;
        private String gstin;
    }
}