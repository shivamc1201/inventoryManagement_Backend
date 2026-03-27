package com.nector.userservice.dto.invoice;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long orderId;
    private Long orderConfirmationId;
    private Long distributorId;
    private String distributorName;
    private String gdnNumber;
    private LocalDateTime invoiceDate;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal taxAmount;
    private java.math.BigDecimal grandTotal;
    private String invoiceStatus;
    private String pdfUrl;
    private String paymentTerms;
    private String remarks;
    private LocalDateTime createdAt;
    private Boolean hasPdf;
}
