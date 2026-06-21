package com.nector.userservice.dto.invoice;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProformaInvoice {
    private String piNumber;
    private LocalDate piDate;
    private LocalDate createdAt;
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
    private String taxInWords;
    
    // Additional fields for template
    private String deliveryNote;
    private String orderNo;
    private LocalDate orderDate;
    private String dispatchDocNo;
    private LocalDate deliveryNoteDate;
    private String totalAltQty;
    private String totalQty;
    
    @Data
    public static class BuyerDetails {
        private String name;
        private String address;
        private String pincode;
        private String state;
        private String stateCode;
        private String gstin;
        private String contact;
    }
}