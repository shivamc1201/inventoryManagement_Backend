package com.nector.userservice.service;

import com.nector.userservice.ordertracking.entity.OrderDocument;
import com.nector.userservice.ordertracking.entity.OrderTracking;
import com.nector.userservice.ordertracking.repository.OrderDocumentRepository;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import com.nector.userservice.ordertracking.service.OrderDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration service to connect document generation (PI, GDN, Invoice) 
 * with Order Tracking system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDocumentIntegrationService {

    private final OrderDocumentService orderDocumentService;
    private final OrderDocumentRepository orderDocumentRepository;
    private final OrderTrackingRepository orderTrackingRepository;

    /**
     * Save Proforma Invoice reference in order_documents table
     * Called when PI is generated (Step 4 completion)
     */
    @Transactional
    public void saveProformaInvoiceReference(Long cartId, String piNumber, String pdfUrl) {
        try {
            // Find order tracking by cart ID
            String orderNumber = "ORD-" + cartId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            OrderTracking order = orderTrackingRepository.findByOrderNumber(orderNumber);
            if (order == null) {
                log.warn("Order tracking not found for cart {} when saving PI reference", cartId);
                return;
            }

            // Save document reference
            OrderDocument doc = orderDocumentService.saveDocument(
                order.getId(),
                "PI",
                piNumber + ".pdf",
                pdfUrl // storage path is the Cloudinary URL
            );
            
            log.info("PI reference saved: OrderTracking ID={}, PI Number={}, Document ID={}", 
                order.getId(), piNumber, doc.getId());
                
        } catch (Exception e) {
            log.error("Failed to save PI reference for cart {}: {}", cartId, e.getMessage());
            // Don't throw - don't break the main PI generation flow
        }
    }

    /**
     * Save GDN reference in order_documents table
     * Called when GDN is generated (Step 9 completion)
     */
    @Transactional
    public void saveGdnReference(Long orderId, String gdnNumber, String pdfUrl) {
        try {
            // Find order tracking by order ID
            String orderNumber = "ORD-" + orderId + "-" + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            OrderTracking order = orderTrackingRepository.findByOrderNumber(orderNumber);
            if (order == null) {
                log.warn("Order tracking not found for order {} when saving GDN reference", orderId);
                return;
            }

            // Save document reference
            OrderDocument doc = orderDocumentService.saveDocument(
                order.getId(),
                "GDN",
                gdnNumber + ".pdf",
                pdfUrl // storage path is the Cloudinary URL
            );
            
            log.info("GDN reference saved: OrderTracking ID={}, GDN Number={}, Document ID={}", 
                order.getId(), gdnNumber, doc.getId());
                
        } catch (Exception e) {
            log.error("Failed to save GDN reference for order {}: {}", orderId, e.getMessage());
            // Don't throw - don't break the main GDN generation flow
        }
    }

    /**
     * Save Invoice reference in order_documents table
     * Called when Invoice is generated (after distributor confirmation)
     */
    @Transactional
    public void saveInvoiceReference(Long orderConfirmationId, String invoiceNumber, String pdfUrl) {
        try {
            // Get order confirmation to find the order ID
            // This would need repository access to order_confirmation table
            // For now, assuming we can derive order ID from confirmation ID
            
            // Find order tracking (this would need to be implemented based on your flow)
            // OrderTracking order = orderTrackingRepository.findByOrderConfirmationId(orderConfirmationId);
            
            // For demonstration, using a placeholder approach
            log.info("Invoice reference would be saved: Invoice Number={}, URL={}", invoiceNumber, pdfUrl);
            
            // Save document reference
            // OrderDocument doc = orderDocumentService.saveDocument(
            //     order.getId(),
            //     "INVOICE", 
            //     invoiceNumber + ".pdf",
            //     pdfUrl
            // );
            
        } catch (Exception e) {
            log.error("Failed to save Invoice reference for confirmation {}: {}", orderConfirmationId, e.getMessage());
            // Don't throw - don't break the main invoice generation flow
        }
    }

    /**
     * Get document info for order tracking step download
     */
    public boolean hasDocument(Long orderId, String docType) {
        try {
            return orderDocumentRepository.existsByOrderIdAndDocType(orderId, docType);
        } catch (Exception e) {
            log.error("Error checking document existence for order {} type {}: {}", orderId, docType, e.getMessage());
            return false;
        }
    }
}
