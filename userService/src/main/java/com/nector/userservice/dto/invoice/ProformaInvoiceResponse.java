package com.nector.userservice.dto.invoice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProformaInvoiceResponse {
    private Long id;
    private String piNumber;
    private Long cartId;
    private Long userId;
    private LocalDateTime piDate;
    
    // Seller Details
    private String sellerCompanyName;
    private String sellerAddress;
    private String sellerGstin;
    private String sellerCin;
    private String sellerContactNumber;
    private String sellerEmail;
    
    // Buyer Details
    private String buyerName;
    private String buyerBillingAddress;
    private String buyerShippingAddress;
    private String buyerState;
    private String buyerStateCode;
    private String buyerGstin;
    private String buyerContactNumber;
    
    // Invoice Metadata
    private String modeOfPayment;
    private String buyerOrderNumber;
    private String deliveryNote;
    private String dispatchDocumentNumber;
    private String termsOfDelivery;
    
    // Totals
    private BigDecimal subtotal;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal grandTotal;
    private String amountInWords;
    
    private List<ProformaInvoiceItemResponse> items;
}

