package com.nector.userservice.dto.invoice;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class Invoice {
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String paymentTerms;
    private String modeOfPayment;
    private String deliveryNote;
    private String orderDate;
    private String dispatchDocNo;
    private String deliveryNoteDate;
    private String billOfLading;
    private String motorVehicleNo;
    
    // Seller details
    private String companyName;
    private String companyAddress;
    private String gstin;
    private String contactNumber;
    private String email;
    private String panNumber;
    private String bankDetails;
    
    // Buyer details
    private BuyerDetails billTo;
    private BuyerDetails shipTo;
    
    // Order details
    private String orderNo;
    private String gdnNumber;
    private String orderConfirmationRemarks;
    
    // Items
    private List<InvoiceItem> items;
    
    // Financial details
    private double subtotal;
    private double taxAmount;
    private double grandTotal;
    private String totalQty;
    private String totalAltQty;
    private String amountInWords;
    private String taxInWords;
    
    // Footer details
    private String termsAndConditions;
    private String authorizedSignatory;
    
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
