package com.nector.userservice.service;

import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.dto.invoice.Invoice;
import com.nector.userservice.dto.invoice.InvoiceItem;
import com.nector.userservice.interceptors.distributor.model.OrderConfirmation;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.distributor.repository.OrderConfirmationRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final CartRepository cartRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderConfirmationRepository orderConfirmationRepository;
    private final TemplateEngine templateEngine;
    private final HtmlToPdfService htmlToPdfService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final DistributorRepository distributorRepository;

    @Transactional
    public String generateInvoice(Long orderConfirmationId) {
        log.info("=== INVOICE GENERATION STARTED ===");
        log.info("Request details - Order Confirmation ID: {}, Timestamp: {}", orderConfirmationId, java.time.LocalDateTime.now());

        try {
            // Step 1: Fetch order confirmation
            log.info("Step 1/6: Fetching order confirmation for ID: {}", orderConfirmationId);
            OrderConfirmation orderConfirmation = orderConfirmationRepository.findById(orderConfirmationId)
                    .orElseThrow(() -> new RuntimeException("Order confirmation not found with ID: " + orderConfirmationId));

            log.info("Order confirmation found - ID: {}, Order ID: {}, Distributor ID: {}, Status: {}",
                    orderConfirmation.getId(), orderConfirmation.getOrderId(), orderConfirmation.getDistributorId(), orderConfirmation.getStatus());

            // Step 2: Fetch cart
            log.info("Step 2/6: Fetching cart details for order ID: {}", orderConfirmation.getOrderId());
            Cart cart = cartRepository.findById(orderConfirmation.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + orderConfirmation.getOrderId()));

            log.info("Cart found - ID: {}, Distributor ID: {}, Status: {}, Items: {}",
                    cart.getId(), cart.getDistributorId(), cart.getStatus(), cart.getCartItems().size());

            // Step 3: Create Invoice entity
            log.info("Step 3/6: Creating Invoice entity in database");
            com.nector.userservice.model.Invoice invoiceEntity = createInvoiceEntity(orderConfirmation, cart);
            invoiceRepository.save(invoiceEntity);
            log.info("Invoice entity created - ID: {}, Invoice Number: {}, Amount: {}",
                    invoiceEntity.getId(), invoiceEntity.getInvoiceNumber(), invoiceEntity.getGrandTotal());

            // Step 4: Generate invoice data
            log.info("Step 4/6: Generating invoice data from cart and order confirmation");
            Invoice invoice = createInvoiceFromData(orderConfirmation, cart);
            log.info("Invoice data created - Invoice Number: {}, Items: {}, Total Amount: {}",
                    invoice.getInvoiceNumber(), invoice.getItems().size(), invoice.getGrandTotal());

            // Step 5: Generate HTML
            log.info("Step 5/6: Converting invoice to HTML template");
            String html = generateHtmlFromTemplate(invoice);
            log.info("HTML generated successfully - Length: {} characters", html.length());

            // Step 6: Convert to PDF
            log.info("Step 6/6: Converting HTML to PDF bytes");
            byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
            log.info("PDF generated successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            // Step 7: Upload to Cloudinary
            log.info("Step 7/7: Uploading PDF to Cloudinary");
            String cloudinaryUrl = uploadPdfToCloudinary(pdfBytes, invoice.getInvoiceNumber());

            // Update entity with Cloudinary URL
            invoiceEntity.setPdfUrl(cloudinaryUrl);
            invoiceRepository.save(invoiceEntity);

            log.info("=== INVOICE GENERATION COMPLETED ===");
            log.info("Success - Invoice Number: {}, Cloudinary URL: {}, PDF Size: {} KB",
                    invoice.getInvoiceNumber(), cloudinaryUrl, pdfBytes.length / 1024.0);
            log.info("Entity updated - ID: {}, PDF URL stored: {}", invoiceEntity.getId(), invoiceEntity.getPdfUrl());

            return cloudinaryUrl;

        } catch (Exception e) {
            log.error("=== INVOICE GENERATION FAILED ===");
            log.error("Failure details - Order Confirmation ID: {}, Error: {}, Timestamp: {}",
                    orderConfirmationId, e.getMessage(), java.time.LocalDateTime.now());
            log.error("Stack trace:", e);
            throw new RuntimeException("Invoice generation failed", e);
        }
    }

    private String uploadPdfToCloudinary(byte[] pdfBytes, String invoiceNumber) {
        File tempFile = null;
        try {
            // Create temporary file
            tempFile = createTempPdfFile(pdfBytes, invoiceNumber);

            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryStorageService.uploadPdf(tempFile);

            log.info("PDF uploaded to Cloudinary: {} -> {}", invoiceNumber, cloudinaryUrl);
            return cloudinaryUrl;

        } catch (Exception e) {
            log.error("Failed to upload PDF to Cloudinary: {} - {}", invoiceNumber, e.getMessage());
            throw new RuntimeException("PDF upload to Cloudinary failed", e);
        } finally {
            // Always cleanup temporary file
            cleanupTempFile(tempFile);
        }
    }

    private File createTempPdfFile(byte[] pdfBytes, String invoiceNumber) throws IOException {
        // Create temp directory if not exists
        Path tempDir = Path.of("temp");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Create temp file
        String tempFileName = invoiceNumber.replace("/", "-") + "_" + System.currentTimeMillis() + ".pdf";
        File tempFile = tempDir.resolve(tempFileName).toFile();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }

        log.debug("Created temporary PDF file: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            try {
                Files.deleteIfExists(tempFile.toPath());
                log.debug("Cleaned up temporary file: {}", tempFile.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to cleanup temporary file: {} - {}",
                        tempFile.getAbsolutePath(), e.getMessage());
            }
        }
    }

    private com.nector.userservice.model.Invoice createInvoiceEntity(OrderConfirmation orderConfirmation, Cart cart) {
        com.nector.userservice.model.Invoice invoiceEntity = new com.nector.userservice.model.Invoice();
        invoiceEntity.setInvoiceNumber("INV-" + orderConfirmation.getOrderId() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        invoiceEntity.setOrderId(orderConfirmation.getOrderId());
        invoiceEntity.setOrderConfirmationId(orderConfirmation.getId());
        invoiceEntity.setDistributorId(orderConfirmation.getDistributorId());
        invoiceEntity.setGdnNumber(orderConfirmation.getGdnNumber());
        invoiceEntity.setRemarks(orderConfirmation.getRemarks());

        if (orderConfirmation.getDistributorId() != null) {
            distributorRepository.findById(orderConfirmation.getDistributorId())
                    .ifPresent(distributor -> invoiceEntity.setDistributorName(distributor.getFirstName()));
        }

        // Calculate total amount based on received quantities
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        if (orderConfirmation.getItemConfirmations() != null) {
            for (var itemConfirmation : orderConfirmation.getItemConfirmations()) {
                if (itemConfirmation.getReceivedQuantity() != null && itemConfirmation.getReceivedQuantity() > 0) {
                    // Find the corresponding cart item to get price
                    CartItem cartItem = cart.getCartItems().stream()
                            .filter(ci -> ci.getItem().getId().equals(itemConfirmation.getItemId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (cartItem != null) {
                        totalAmount = totalAmount.add(
                            cartItem.getPriceAtTime().multiply(java.math.BigDecimal.valueOf(itemConfirmation.getReceivedQuantity()))
                        );
                    }
                }
            }
        }

        invoiceEntity.setTotalAmount(totalAmount);
        invoiceEntity.setTaxAmount(totalAmount.multiply(java.math.BigDecimal.valueOf(0.18))); // 18% GST
        invoiceEntity.setGrandTotal(totalAmount.add(invoiceEntity.getTaxAmount()));
        invoiceEntity.setInvoiceStatus(com.nector.userservice.model.Invoice.InvoiceStatus.GENERATED);
        
        log.info("Invoice entity created with status: {} for invoice: {}", 
                invoiceEntity.getInvoiceStatus(), invoiceEntity.getInvoiceNumber());

        return invoiceEntity;
    }

    private Invoice createInvoiceFromData(OrderConfirmation orderConfirmation, Cart cart) {
        Invoice invoice = new Invoice();

        // Invoice details
        invoice.setInvoiceNumber("INV-" + orderConfirmation.getOrderId() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setPaymentTerms("Due on Receipt");
        invoice.setOrderNo(String.valueOf(orderConfirmation.getOrderId()));
        invoice.setGdnNumber(orderConfirmation.getGdnNumber());
        invoice.setOrderConfirmationRemarks(orderConfirmation.getRemarks());

        // Seller details
        invoice.setCompanyName("Nectar Origin Private Limited");
        invoice.setCompanyAddress("360 K, Shiv Parwati Nagar, Block Road No-2, Ward No 16, Kahalgaon, Bhagalpur Bihar, Bihar - 813203, India");
        invoice.setGstin("U74999BR2016PTC032690");
        invoice.setContactNumber("06429-450126,9797979522");
        invoice.setEmail("nectarorigin@gmail.com");
        invoice.setPanNumber("AAFCN7425K");
        invoice.setBankDetails("Bank of Baroda, Kahalgaon Branch\nA/c No: 1234567890\nIFSC: BARB0KAHXXX");

        // Buyer details from distributor
        var distributor = distributorRepository.findById(orderConfirmation.getDistributorId()).orElse(null);
        log.info("Distributor found for ID {}: {}", orderConfirmation.getDistributorId(), distributor != null ? distributor.getFirstName() : "NOT FOUND");
        if (distributor != null) {
            // Bill To details
            Invoice.BuyerDetails billTo = new Invoice.BuyerDetails();
            billTo.setName(distributor.getFirstName());
            billTo.setAddress(distributor.getAddress());
            billTo.setGstin(distributor.getGstNumber());
            billTo.setState("Bihar");
            billTo.setStateCode("10");
            invoice.setBillTo(billTo);

            // Ship To details (same as bill to for now)
            Invoice.BuyerDetails shipTo = new Invoice.BuyerDetails();
            shipTo.setName(distributor.getFirstName());
            shipTo.setAddress(cart.getAddress() != null ? cart.getAddress() : distributor.getAddress());
            shipTo.setGstin(distributor.getGstNumber());
            shipTo.setState("Bihar");
            shipTo.setStateCode("10");
            invoice.setShipTo(shipTo);
            log.info("ShipTo set with name: {}", shipTo.getName());
        } else {
            log.error("Distributor not found for ID: {}, creating fallback buyer details", orderConfirmation.getDistributorId());
            // Create fallback buyer details
            Invoice.BuyerDetails billTo = new Invoice.BuyerDetails();
            billTo.setName("Customer Name");
            billTo.setAddress(cart.getAddress() != null ? cart.getAddress() : "Customer Address");
            billTo.setGstin("Customer GSTIN");
            billTo.setState("Bihar");
            billTo.setStateCode("10");
            invoice.setBillTo(billTo);

            Invoice.BuyerDetails shipTo = new Invoice.BuyerDetails();
            shipTo.setName("Customer Name");
            shipTo.setAddress(cart.getAddress() != null ? cart.getAddress() : "Customer Address");
            shipTo.setGstin("Customer GSTIN");
            shipTo.setState("Bihar");
            shipTo.setStateCode("10");
            invoice.setShipTo(shipTo);
        }

        // Items from order confirmation (received quantities)
        List<InvoiceItem> items = IntStream.range(0, orderConfirmation.getItemConfirmations().size())
                .mapToObj(i -> {
                    var itemConfirmation = orderConfirmation.getItemConfirmations().get(i);
                    if (itemConfirmation.getReceivedQuantity() == null || itemConfirmation.getReceivedQuantity() <= 0) {
                        return null;
                    }

                    // Find the corresponding cart item to get price and name
                    CartItem cartItem = cart.getCartItems().stream()
                            .filter(ci -> ci.getItem().getId().equals(itemConfirmation.getItemId()))
                            .findFirst()
                            .orElse(null);

                    if (cartItem == null) {
                        log.warn("Cart item not found for item ID: {}", itemConfirmation.getItemId());
                        return null;
                    }

                    InvoiceItem item = new InvoiceItem();
                    item.setSrNo(i + 1);
                    item.setDescription(cartItem.getItem().getName());
                    item.setHsnCode("1234"); // Default HSN
                    item.setQuantity(itemConfirmation.getReceivedQuantity());
                    item.setRatePerUnit(cartItem.getPriceAtTime().doubleValue());
                    item.setUnit("Pcs");
                    item.setAmount(cartItem.getPriceAtTime().doubleValue() * itemConfirmation.getReceivedQuantity());
                    return item;
                })
                .filter(item -> item != null)
                .toList();

        invoice.setItems(items);

        // Calculate totals
        double subtotal = items.stream().mapToDouble(InvoiceItem::getAmount).sum();
        double taxAmount = subtotal * 0.18; // 18% GST
        double grandTotal = subtotal + taxAmount;
        int totalQuantity = items.stream().mapToInt(InvoiceItem::getQuantity).sum();

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setGrandTotal(grandTotal);
        invoice.setTotalQty(String.valueOf(totalQuantity));
        invoice.setAmountInWords(convertToWords(grandTotal));

        // Additional details
        invoice.setTermsAndConditions("1. Goods once sold will not be taken back.\n2. Payment should be made as per the terms agreed.\n3. Subject to Bihar Jurisdiction.");
        invoice.setAuthorizedSignatory("Authorized Signatory");

        return invoice;
    }

    private String generateHtmlFromTemplate(Invoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        return templateEngine.process("invoice", context);
    }

    private String convertToWords(double amount) {
        return String.format("%.0f Rupees Only", amount);
    }

    /**
     * Get Invoice details by order confirmation ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInvoiceDetailsByOrderConfirmation(Long orderConfirmationId) {
        log.info("=== GET INVOICE DETAILS ===");
        log.info("Request - Order Confirmation ID: {}, Timestamp: {}", orderConfirmationId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.Invoice invoice = invoiceRepository.findByOrderConfirmationId(orderConfirmationId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found for order confirmation ID: " + orderConfirmationId));

            log.info("Invoice found - ID: {}, Invoice Number: {}, Amount: {}, PDF URL: {}",
                    invoice.getId(), invoice.getInvoiceNumber(), invoice.getGrandTotal(), invoice.getPdfUrl());

            Map<String, Object> response = Map.ofEntries(
                    Map.entry("id", invoice.getId()),
                    Map.entry("invoiceNumber", invoice.getInvoiceNumber()),
                    Map.entry("orderId", invoice.getOrderId()),
                    Map.entry("orderConfirmationId", invoice.getOrderConfirmationId()),
                    Map.entry("distributorId", invoice.getDistributorId()),
                    Map.entry("distributorName", invoice.getDistributorName() != null ? invoice.getDistributorName() : ""),
                    Map.entry("gdnNumber", invoice.getGdnNumber() != null ? invoice.getGdnNumber() : ""),
                    Map.entry("totalAmount", invoice.getTotalAmount()),
                    Map.entry("taxAmount", invoice.getTaxAmount()),
                    Map.entry("grandTotal", invoice.getGrandTotal()),
                    Map.entry("invoiceStatus", invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus().toString() : "UNKNOWN"),
                    Map.entry("invoiceDate", invoice.getInvoiceDate()),
                    Map.entry("pdfUrl", invoice.getPdfUrl()),
                    Map.entry("hasPdf", invoice.getPdfUrl() != null && !invoice.getPdfUrl().isEmpty()),
                    Map.entry("paymentTerms", invoice.getPaymentTerms() != null ? invoice.getPaymentTerms() : ""),
                    Map.entry("remarks", invoice.getRemarks() != null ? invoice.getRemarks() : ""),
                    Map.entry("createdAt", invoice.getCreatedAt())
            );

            log.info("Invoice details sent successfully for order confirmation ID: {}", orderConfirmationId);
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve Invoice for order confirmation ID: {} - {}", orderConfirmationId, e.getMessage());
            throw new RuntimeException("Invoice not found", e);
        }
    }

    /**
     * Get Invoice details by order ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInvoiceDetailsByOrder(Long orderId) {
        log.info("=== GET INVOICE DETAILS BY ORDER ===");
        log.info("Request - Order ID: {}, Timestamp: {}", orderId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.Invoice invoice = invoiceRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found for order ID: " + orderId));

            log.info("Invoice found - ID: {}, Invoice Number: {}, Amount: {}, PDF URL: {}",
                    invoice.getId(), invoice.getInvoiceNumber(), invoice.getGrandTotal(), invoice.getPdfUrl());

            // Debug log for potential null fields
            log.info("Debug - Invoice fields: status={}, date={}, paymentTerms={}, remarks={}, createdAt={}",
                    invoice.getInvoiceStatus(), invoice.getInvoiceDate(), invoice.getPaymentTerms(), 
                    invoice.getRemarks(), invoice.getCreatedAt());

            Map<String, Object> response = Map.ofEntries(
                    Map.entry("id", invoice.getId()),
                    Map.entry("invoiceNumber", invoice.getInvoiceNumber()),
                    Map.entry("orderId", invoice.getOrderId()),
                    Map.entry("orderConfirmationId", invoice.getOrderConfirmationId()),
                    Map.entry("distributorId", invoice.getDistributorId()),
                    Map.entry("distributorName", invoice.getDistributorName() != null ? invoice.getDistributorName() : ""),
                    Map.entry("gdnNumber", invoice.getGdnNumber() != null ? invoice.getGdnNumber() : ""),
                    Map.entry("totalAmount", invoice.getTotalAmount()),
                    Map.entry("taxAmount", invoice.getTaxAmount()),
                    Map.entry("grandTotal", invoice.getGrandTotal()),
                    Map.entry("invoiceStatus", invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus().toString() : "UNKNOWN"),
                    Map.entry("invoiceDate", invoice.getInvoiceDate()),
                    Map.entry("pdfUrl", invoice.getPdfUrl()),
                    Map.entry("hasPdf", invoice.getPdfUrl() != null && !invoice.getPdfUrl().isEmpty()),
                    Map.entry("paymentTerms", invoice.getPaymentTerms() != null ? invoice.getPaymentTerms() : ""),
                    Map.entry("remarks", invoice.getRemarks() != null ? invoice.getRemarks() : ""),
                    Map.entry("createdAt", invoice.getCreatedAt())
            );

            log.info("Invoice details sent successfully for order ID: {}", orderId);
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve Invoice for order ID: {} - {}", orderId, e.getMessage());
            throw new RuntimeException("Invoice not found", e);
        }
    }

    /**
     * Download Invoice PDF from Cloudinary
     */
    public byte[] downloadInvoicePdf(Long orderId) {
        log.info("=== DOWNLOAD INVOICE PDF ===");
        log.info("Download request - Order ID: {}, Timestamp: {}", orderId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.Invoice invoice = invoiceRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found for order ID: " + orderId));

            log.info("Invoice found - ID: {}, Invoice Number: {}", invoice.getId(), invoice.getInvoiceNumber());

            // Check if PDF URL exists
            if (invoice.getPdfUrl() == null || invoice.getPdfUrl().isEmpty()) {
                log.error("PDF URL not available for Invoice ID: {}", invoice.getId());
                throw new RuntimeException("PDF not available for Invoice: " + invoice.getInvoiceNumber());
            }

            log.info("PDF URL found: {}", invoice.getPdfUrl());

            // Download using Cloudinary's resource API
            byte[] pdfBytes = cloudinaryStorageService.downloadPdfByUrl(invoice.getPdfUrl());

            log.info("PDF downloaded successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            return pdfBytes;

        } catch (Exception e) {
            log.error("Failed to download PDF from Cloudinary for order ID: {} - {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to download PDF", e);
        }
    }

    /**
     * Get all invoices for a distributor
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInvoicesByDistributor(Long distributorId) {
        log.info("=== GET DISTRIBUTOR INVOICES ===");
        log.info("Request - Distributor ID: {}, Timestamp: {}", distributorId, java.time.LocalDateTime.now());

        try {
            List<com.nector.userservice.model.Invoice> invoices = invoiceRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);

            List<Map<String, Object>> response = invoices.stream()
                    .map(invoice -> {
                        Map<String, Object> invoiceMap = new HashMap<>();
                        invoiceMap.put("id", invoice.getId());
                        invoiceMap.put("invoiceNumber", invoice.getInvoiceNumber());
                        invoiceMap.put("orderId", invoice.getOrderId());
                        invoiceMap.put("orderConfirmationId", invoice.getOrderConfirmationId());
                        invoiceMap.put("distributorId", invoice.getDistributorId());
                        invoiceMap.put("distributorName", invoice.getDistributorName());
                        invoiceMap.put("gdnNumber", invoice.getGdnNumber());
                        invoiceMap.put("totalAmount", invoice.getTotalAmount());
                        invoiceMap.put("taxAmount", invoice.getTaxAmount());
                        invoiceMap.put("grandTotal", invoice.getGrandTotal());
                        invoiceMap.put("invoiceStatus", invoice.getInvoiceStatus().toString());
                        invoiceMap.put("invoiceDate", invoice.getInvoiceDate());
                        invoiceMap.put("pdfUrl", invoice.getPdfUrl());
                        invoiceMap.put("hasPdf", invoice.getPdfUrl() != null && !invoice.getPdfUrl().isEmpty());
                        invoiceMap.put("paymentTerms", invoice.getPaymentTerms());
                        invoiceMap.put("remarks", invoice.getRemarks());
                        invoiceMap.put("createdAt", invoice.getCreatedAt());
                        return invoiceMap;
                    })
                    .toList();

            log.info("Retrieved {} invoices for distributor ID: {}", response.size(), distributorId);
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve invoices for distributor ID: {} - {}", distributorId, e.getMessage());
            throw new RuntimeException("Failed to retrieve invoices", e);
        }
    }
}
