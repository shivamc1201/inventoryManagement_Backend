package com.nector.userservice.service;

import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.dto.invoice.InvoiceItem;
import com.nector.userservice.dto.invoice.ProformaInvoice;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.ProformaInvoiceRepository;
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
public class ProformaInvoiceService {

    private final CartRepository cartRepository;
    private final ProformaInvoiceRepository proformaInvoiceRepository;
    private final TemplateEngine templateEngine;
    private final HtmlToPdfService htmlToPdfService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final DocumentNumberService documentNumberService;
    private final DistributorRepository distributorRepository;
    private final OrderTrackingRepository orderTrackingRepository;


    @Transactional
    public String generateProformaInvoice(Long cartId) {
        log.info("=== PROFORMA INVOICE GENERATION STARTED ===");
        log.info("Request details - Cart ID: {}, Timestamp: {}", cartId, java.time.LocalDateTime.now());

        try {
            // Step 1: Fetch cart
            log.info("Step 1/6: Fetching cart details for ID: {}", cartId);
            Cart cart = cartRepository.findByIdWithItems(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));

            log.info("Cart found - ID: {}, Distributor ID: {}, Status: {}, Items: {}",
                    cart.getId(), cart.getDistributorId(), cart.getStatus(), cart.getCartItems().size());

            // Generate PI number once (format: EI/YYYY-YY/NNNN)
            String piNumber = documentNumberService.generateProformaInvoiceNumber();
            log.info("Generated PI Number: {}", piNumber);

            // Step 2: Create PI entity
            log.info("Step 2/6: Creating Proforma Invoice entity in database");
            com.nector.userservice.model.ProformaInvoice piEntity = createProformaInvoiceEntity(cart, piNumber);
            proformaInvoiceRepository.save(piEntity);
            log.info("PI entity created - ID: {}, PI Number: {}, Amount: {}",
                    piEntity.getId(), piEntity.getPiNumber(), piEntity.getAmount());

            // Step 3: Generate invoice data
            log.info("Step 3/6: Generating invoice data from cart");
            ProformaInvoice invoice = createInvoiceFromCart(cart, piNumber);
            log.info("Invoice data created - PI Number: {}, Items: {}, Total Amount: {}",
                    invoice.getPiNumber(), invoice.getItems().size(), invoice.getGrandTotal());

            // Step 4: Generate HTML
            log.info("Step 4/6: Converting invoice to HTML template");
            String html = generateHtmlFromTemplate(invoice);
            log.info("HTML generated successfully - Length: {} characters", html.length());

            // Step 5: Convert to PDF
            log.info("Step 5/6: Converting HTML to PDF bytes");
            byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
            log.info("PDF generated successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            // Step 6: Upload to Cloudinary
            log.info("Step 6/6: Uploading PDF to Cloudinary");
            String cloudinaryUrl = uploadPdfToCloudinary(pdfBytes, invoice.getPiNumber());

            // Update entity with Cloudinary URL
            piEntity.setPdfUrl(cloudinaryUrl);
            proformaInvoiceRepository.save(piEntity);

            log.info("=== PROFORMA INVOICE GENERATION COMPLETED ===");
            log.info("Success - PI Number: {}, Cloudinary URL: {}, PDF Size: {} KB",
                    invoice.getPiNumber(), cloudinaryUrl, pdfBytes.length / 1024.0);
            log.info("Entity updated - ID: {}, PDF URL stored: {}", piEntity.getId(), piEntity.getPdfUrl());

            return cloudinaryUrl;

        } catch (Exception e) {
            log.error("=== PROFORMA INVOICE GENERATION FAILED ===");
            log.error("Failure details - Cart ID: {}, Error: {}, Timestamp: {}",
                    cartId, e.getMessage(), java.time.LocalDateTime.now());
            log.error("Stack trace:", e);
            throw new RuntimeException("Proforma invoice generation failed", e);
        }
    }







    private String uploadPdfToCloudinary(byte[] pdfBytes, String piNumber) {
        File tempFile = null;
        try {
            // Create temporary file
            tempFile = createTempPdfFile(pdfBytes, piNumber);

            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryStorageService.uploadPdf(tempFile);

            log.info("PDF uploaded to Cloudinary: {} -> {}", piNumber, cloudinaryUrl);
            return cloudinaryUrl;

        } catch (Exception e) {
            log.error("Failed to upload PDF to Cloudinary: {} - {}", piNumber, e.getMessage());
            throw new RuntimeException("PDF upload to Cloudinary failed", e);
        } finally {
            // Always cleanup temporary file
            cleanupTempFile(tempFile);
        }
    }

    private File createTempPdfFile(byte[] pdfBytes, String piNumber) throws IOException {
        // Create temp directory if not exists
        Path tempDir = Path.of("temp");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Create temp file
        String tempFileName = piNumber.replace("/", "-") + "_" + System.currentTimeMillis() + ".pdf";
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

    private com.nector.userservice.model.ProformaInvoice createProformaInvoiceEntity(Cart cart, String piNumber) {
        com.nector.userservice.model.ProformaInvoice piEntity = new com.nector.userservice.model.ProformaInvoice();
        
        // Use provided PI number in format: EI/YYYY-YY/NNNN (e.g., EI/2026-27/0001)
        piEntity.setPiNumber(piNumber);
        piEntity.setCartId(cart.getId());
        piEntity.setDistributorId(cart.getDistributorId());

        if (cart.getDistributorId() != null) {
            distributorRepository.findById(cart.getDistributorId())
                    .ifPresent(distributor -> piEntity.setDistributorName(distributor.getFirstName()));
        }

        // Calculate total amount
        java.math.BigDecimal totalAmount = cart.getCartItems().stream()
                .map(item -> item.getPriceAtTime().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        piEntity.setAmount(totalAmount);
        piEntity.setPaymentStatus(com.nector.userservice.model.ProformaInvoice.PaymentStatus.PENDING);

        return piEntity;
    }

    // Keep existing methods unchanged
    private ProformaInvoice createInvoiceFromCart(Cart cart, String piNumber) {
        ProformaInvoice invoice = new ProformaInvoice();

        // Invoice details - use provided PI number
        invoice.setPiNumber(piNumber);
        invoice.setPiDate(LocalDate.now());
        invoice.setModeOfPayment("Bank Transfer");

        // Seller details
        invoice.setCompanyName("Nectar Origin Private Limited");
        invoice.setCompanyAddress("360 K, Shiv Parwati Nagar, Block Road No-2, Ward No 16, Kahalgaon, Bhagalpur Bihar, Bihar - 813203, India");
        invoice.setGstin("U74999BR2016PTC032690");
        invoice.setContactNumber("06429-450126,9797979522");
        invoice.setEmail("nectarorigin@gmail.com");

        // Buyer details from distributor
        var distributor = distributorRepository.findById(cart.getDistributorId()).orElse(null);
        log.info("Distributor found for ID {}: {}", cart.getDistributorId(), distributor != null ? distributor.getFirstName() : "NOT FOUND");
        if (distributor != null) {
            // Bill To details
            ProformaInvoice.BuyerDetails billTo = new ProformaInvoice.BuyerDetails();
            billTo.setName(distributor.getFirstName());
            billTo.setAddress(distributor.getAddress());
            billTo.setGstin(distributor.getGstNumber());
            billTo.setState("Bihar"); // You may want to add state to distributor entity
            billTo.setStateCode("10"); // Bihar state code
            billTo.setPincode("813203"); // Default pincode
            billTo.setContact(distributor.getPhoneNumber() != null ? distributor.getPhoneNumber() : "06429-450126");
            invoice.setBillTo(billTo);

            // Ship To details (same as bill to for now, using cart address if available)
            ProformaInvoice.BuyerDetails shipTo = new ProformaInvoice.BuyerDetails();
            shipTo.setName(distributor.getFirstName());
            shipTo.setAddress(cart.getAddress() != null ? cart.getAddress() : distributor.getAddress());
            shipTo.setGstin(distributor.getGstNumber());
            shipTo.setState("Bihar"); // You may want to add state to distributor entity
            shipTo.setStateCode("10"); // Bihar state code
            shipTo.setPincode("813203"); // Default pincode
            shipTo.setContact(distributor.getPhoneNumber() != null ? distributor.getPhoneNumber() : "06429-450126");
            invoice.setShipTo(shipTo);
            log.info("ShipTo set with name: {}", shipTo.getName());
        } else {
            log.error("Distributor not found for ID: {}, creating fallback buyer details", cart.getDistributorId());
            // Create fallback buyer details when distributor is not found
            ProformaInvoice.BuyerDetails billTo = new ProformaInvoice.BuyerDetails();
            billTo.setName("Customer Name");
            billTo.setAddress(cart.getAddress() != null ? cart.getAddress() : "Customer Address");
            billTo.setGstin("Customer GSTIN");
            billTo.setState("Bihar");
            billTo.setStateCode("10");
            invoice.setBillTo(billTo);

            ProformaInvoice.BuyerDetails shipTo = new ProformaInvoice.BuyerDetails();
            shipTo.setName("Customer Name");
            shipTo.setAddress(cart.getAddress() != null ? cart.getAddress() : "Customer Address");
            shipTo.setGstin("Customer GSTIN");
            shipTo.setState("Bihar");
            shipTo.setStateCode("10");
            invoice.setShipTo(shipTo);
            log.info("Fallback buyer details created");
        }

        // Items from cart
        List<InvoiceItem> items = IntStream.range(0, cart.getCartItems().size())
                .mapToObj(i -> {
                    CartItem cartItem = cart.getCartItems().get(i);
                    InvoiceItem item = new InvoiceItem();
                    item.setSrNo(i + 1);
                    item.setDescription(cartItem.getItem().getName());
                    item.setHsnCode(cartItem.getItem().getHsn() != null ? cartItem.getItem().getHsn() : "");
                    java.math.BigDecimal itemVolume = calculateItemVolume(cartItem);
                    item.setAltQty(itemVolume.stripTrailingZeros().toPlainString());
                    item.setQuantity(cartItem.getQuantity());
                    item.setRatePerUnit(cartItem.getPriceAtTime().doubleValue());
                    item.setUnit("Bag");
                    item.setPer("Bag");
                    item.setAmount(cartItem.getPriceAtTime().doubleValue() * cartItem.getQuantity());
                    return item;
                })
                .toList();

        invoice.setItems(items);

        // Calculate totals
        double subtotal = items.stream().mapToDouble(InvoiceItem::getAmount).sum();
        double cgst = 0.0; // No tax for proforma invoice
        double sgst = 0.0; // No tax for proforma invoice
        double grandTotal = subtotal + cgst + sgst;

        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(0.0);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountInWords(convertToWords(grandTotal));

        double totalTax = cgst + sgst;
        invoice.setTaxInWords("NIL");
        java.math.BigDecimal totalAltQtyVal = items.stream()
                .map(InvoiceItem::getAltQty)
                .filter(s -> s != null && !s.isEmpty())
                .map(java.math.BigDecimal::new)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        invoice.setTotalAltQty(totalAltQtyVal.stripTrailingZeros().toPlainString());
        invoice.setDeliveryNote("");

        // Fetch Sales Order number from OrderTracking
        var orderTracking = orderTrackingRepository.findByCartId(cart.getId());
        if (orderTracking != null && orderTracking.getOrderNumber() != null) {
            invoice.setOrderNo(orderTracking.getOrderNumber());
            log.info("Sales Order number {} found for cart ID: {}", orderTracking.getOrderNumber(), cart.getId());
        } else {
            invoice.setOrderNo("");
            log.warn("No Sales Order number found for cart ID: {}", cart.getId());
        }

        invoice.setOrderDate(LocalDate.now());
        invoice.setDispatchDocNo("");
        invoice.setDeliveryNoteDate(LocalDate.now());
        invoice.setTotalQty(String.valueOf(items.stream().mapToInt(InvoiceItem::getQuantity).sum()));

        return invoice;
    }

    private String generateHtmlFromTemplate(ProformaInvoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("logoPath", htmlToPdfService.getLogoDataUri());
        return templateEngine.process("proforma-invoice", context);
    }

    private String convertToWords(double amount) {
        return com.nector.userservice.util.NumberToWordsUtil.convertToWords(amount);
    }


    /**
     * Get Proforma Invoice details by cart ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProformaInvoiceDetails(Long cartId) {
        log.info("=== GET PROFORMA INVOICE DETAILS ===");
        log.info("Request - Cart ID: {}, Timestamp: {}", cartId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.ProformaInvoice pi = proformaInvoiceRepository.findByCartId(cartId)
                    .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for cart ID: " + cartId));

            log.info("Proforma Invoice found - ID: {}, PI Number: {}, Amount: {}, PDF URL: {}",
                    pi.getId(), pi.getPiNumber(), pi.getAmount(), pi.getPdfUrl());

            Map<String, Object> response = Map.of(
                    "id", pi.getId(),
                    "piNumber", pi.getPiNumber(),
                    "cartId", pi.getCartId(),
                    "distributorId", pi.getDistributorId(),
                    "amount", pi.getAmount(),
                    "paymentStatus", pi.getPaymentStatus().toString(),
                    "createdAt", pi.getCreatedAt(),
                    "pdfUrl", pi.getPdfUrl(),
                    "hasPdf", pi.getPdfUrl() != null && !pi.getPdfUrl().isEmpty()
            );

            log.info("Proforma Invoice details sent successfully for cart ID: {}", cartId);
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve Proforma Invoice for cart ID: {} - {}", cartId, e.getMessage());
            throw new RuntimeException("Proforma Invoice not found", e);
        }
    }

    /**
     * Download Proforma Invoice PDF from Cloudinary
     */
    /**
     * Download Proforma Invoice PDF from Cloudinary
     */


    public byte[] downloadProformaInvoicePdf(Long cartId) {
        log.info("=== DOWNLOAD PROFORMA INVOICE PDF ===");
        log.info("Download request - Cart ID: {}, Timestamp: {}", cartId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.ProformaInvoice pi = proformaInvoiceRepository.findByCartId(cartId)
                    .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for cart ID: " + cartId));

            log.info("Proforma Invoice found - ID: {}, PI Number: {}", pi.getId(), pi.getPiNumber());

            // Check if PDF URL exists
            if (pi.getPdfUrl() == null || pi.getPdfUrl().isEmpty()) {
                log.error("PDF URL not available for Proforma Invoice ID: {}", pi.getId());
                throw new RuntimeException("PDF not available for Proforma Invoice: " + pi.getPiNumber());
            }

            log.info("PDF URL found: {}", pi.getPdfUrl());

            // Extract public ID and use Cloudinary's resource API
            String publicId = extractPublicIdFromUrl(pi.getPdfUrl());
            log.info("Using public ID: {}", publicId);

            // Download using Cloudinary's resource API
            byte[] pdfBytes = cloudinaryStorageService.downloadPdfByUrl(pi.getPdfUrl());;

            log.info("PDF downloaded successfully - Size: {} bytes ({} KB)",
                    pdfBytes.length, pdfBytes.length / 1024.0);

            return pdfBytes;

        } catch (Exception e) {
            log.error("Failed to download PDF from Cloudinary for cart ID: {} - {}", cartId, e.getMessage());
            throw new RuntimeException("Failed to download PDF", e);
        }
    }



    /**
     * Extract public ID from Cloudinary URL
     */
        private String extractPublicIdFromUrl(String cloudinaryUrl) {
        try {
            // URL format: https://res.cloudinary.com/cloud_name/raw/upload/v_timestamp/folder/public_id
            String[] parts = cloudinaryUrl.split("/");

            // Find the index of "upload" and get everything after it (excluding timestamp)
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("upload")) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex + 2 >= parts.length) {
                throw new RuntimeException("Invalid Cloudinary URL format - cannot find upload part");
            }

            // Skip the timestamp (uploadIndex + 1) and build the public_id with folder
            StringBuilder publicIdBuilder = new StringBuilder();
            for (int i = uploadIndex + 2; i < parts.length; i++) {
                if (publicIdBuilder.length() > 0) {
                    publicIdBuilder.append("/");
                }
                String part = parts[i];
                // Remove file extension from the last part
                if (i == parts.length - 1 && part.contains(".")) {
                    part = part.substring(0, part.lastIndexOf('.'));
                }
                publicIdBuilder.append(part);
            }

            String publicId = publicIdBuilder.toString();
            log.info("Extracted public ID: {} from URL: {}", publicId, cloudinaryUrl);
            return publicId;

        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {} - {}", cloudinaryUrl, e.getMessage());
            throw new RuntimeException("Invalid Cloudinary URL format", e);
        }
    }


    /**
     * Get Proforma Invoice PDF URL only
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProformaInvoiceUrl(Long cartId) {
        log.info("=== GET PROFORMA INVOICE URL ===");
        log.info("URL request - Cart ID: {}, Timestamp: {}", cartId, java.time.LocalDateTime.now());

        try {
            com.nector.userservice.model.ProformaInvoice pi = proformaInvoiceRepository.findByCartId(cartId)
                    .orElseThrow(() -> new RuntimeException("Proforma Invoice not found for cart ID: " + cartId));

            if (pi.getPdfUrl() == null || pi.getPdfUrl().isEmpty()) {
                log.error("PDF URL not available for Proforma Invoice ID: {}", pi.getId());
                throw new RuntimeException("PDF URL not available for Proforma Invoice: " + pi.getPiNumber());
            }

            log.info("PDF URL retrieved successfully - Cart ID: {}, PI Number: {}, URL: {}",
                    cartId, pi.getPiNumber(), pi.getPdfUrl());

            Map<String, Object> response = Map.of(
                    "cartId", cartId,
                    "piNumber", pi.getPiNumber(),
                    "distributorId", pi.getDistributorId(),
                    "distributorName", pi.getDistributorName(), // Add this line
                    "pdfUrl", pi.getPdfUrl(),
                    "amount", pi.getAmount(),
                    "paymentStatus", pi.getPaymentStatus().toString()
            );

            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve PDF URL for cart ID: {} - {}", cartId, e.getMessage());
            throw new RuntimeException("Proforma Invoice not found", e);
        }
    }

    /**
     * Get all Proforma Invoices
     */
// In ProformaInvoiceService.java, update the getAllProformaInvoices method:
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllProformaInvoices() {
        log.info("=== GET ALL PROFORMA INVOICES ===");
        log.info("Request timestamp: {}", java.time.LocalDateTime.now());

        try {
            List<com.nector.userservice.model.ProformaInvoice> invoices = proformaInvoiceRepository.findAll();

            List<Map<String, Object>> response = invoices.stream()
                    .map(pi -> {
                        Map<String, Object> piMap = new HashMap<>();
                        piMap.put("id", pi.getId());
                        piMap.put("piNumber", pi.getPiNumber());
                        piMap.put("cartId", pi.getCartId());
                        piMap.put("distributorId", pi.getDistributorId());
                        piMap.put("distributorName", pi.getDistributorName()); // Add this line
                        piMap.put("amount", pi.getAmount());
                        piMap.put("paymentStatus", pi.getPaymentStatus() != null ? pi.getPaymentStatus().toString() : "PENDING");
                        piMap.put("createdAt", pi.getCreatedAt());
                        piMap.put("pdfUrl", pi.getPdfUrl());
                        piMap.put("hasPdf", pi.getPdfUrl() != null && !pi.getPdfUrl().isEmpty());
                        return piMap;
                    })
                    .toList();

            log.info("Retrieved {} Proforma Invoices successfully", response.size());
            return response;

        } catch (Exception e) {
            log.error("Failed to retrieve all Proforma Invoices: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve Proforma Invoices", e);
        }
    }

    private java.math.BigDecimal calculateItemVolume(CartItem cartItem) {
        FinishedProduct product = cartItem.getItem();
        java.math.BigDecimal weight = product.getWeight();
        int quantity = cartItem.getQuantity();
        
        log.debug("Calculating volume for product: {}, weight: {}, unit: {}, quantity: {}", 
            product.getName(), weight, product.getUnit(), quantity);
        
        if (product.getUnit() == null || weight == null || weight.equals(java.math.BigDecimal.ZERO)) {
            log.warn("Product {} has null weight or unit, returning volume 0", product.getName());
            // Try to extract weight from product name as fallback (e.g., "50 Kg" from "MANKA MAHISHI 50 Kg")
            return extractWeightFromProductName(product.getName(), quantity);
        }
        
        switch (product.getUnit()) {
            case KG:
                return weight.multiply(java.math.BigDecimal.valueOf(quantity))
                        .setScale(6, java.math.RoundingMode.HALF_UP);

            case LITER:
                return weight.multiply(java.math.BigDecimal.valueOf(quantity))
                        .setScale(6, java.math.RoundingMode.HALF_UP);

            case DOZEN:
                java.math.BigDecimal weightPerPiece = weight.divide(java.math.BigDecimal.valueOf(12), 6, java.math.RoundingMode.HALF_UP);
                return weightPerPiece.multiply(java.math.BigDecimal.valueOf(quantity))
                        .setScale(6, java.math.RoundingMode.HALF_UP);

            case PIECES:
                return weight.multiply(java.math.BigDecimal.valueOf(quantity))
                        .setScale(6, java.math.RoundingMode.HALF_UP);

            default:
                return weight.multiply(java.math.BigDecimal.valueOf(quantity))
                        .setScale(6, java.math.RoundingMode.HALF_UP);
        }
    }

    private java.math.BigDecimal extractWeightFromProductName(String productName, int quantity) {
        try {
            // Extract weight from product name (e.g., "50 Kg" from "MANKA MAHISHI 50 Kg")
            String[] words = productName.split(" ");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].matches("\\d+(\\.\\d+)?") && 
                    (words[i + 1].equalsIgnoreCase("Kg") || words[i + 1].equalsIgnoreCase("KG"))) {
                    java.math.BigDecimal extractedWeight = new java.math.BigDecimal(words[i]);
                    return extractedWeight.multiply(java.math.BigDecimal.valueOf(quantity))
                            .setScale(6, java.math.RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract weight from product name: {} - {}", productName, e.getMessage());
        }
        
        // Fallback to zero if extraction fails
        return java.math.BigDecimal.ZERO;
    }

}
