package com.nector.userservice.service;

import com.nector.userservice.cloudinary.CloudinaryStorageService;
import com.nector.userservice.dto.DealerInvoiceDto;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.DealerInvoice;
import com.nector.userservice.model.DealerOrder;
import com.nector.userservice.repository.DealerInvoiceRepository;
import com.nector.userservice.repository.DealerOrderRepository;
import com.nector.userservice.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerInvoiceService {

    private final DealerInvoiceRepository dealerInvoiceRepository;
    private final DealerOrderRepository dealerOrderRepository;
    private final DealerRepository dealerRepository;
    private final DistributorRepository distributorRepository;
    private final DocumentNumberService documentNumberService;
    private final TemplateEngine templateEngine;
    private final HtmlToPdfService htmlToPdfService;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public String generateInvoice(DealerOrder order, Dealer dealer) {
        log.info("=== DEALER INVOICE GENERATION STARTED === Order ID: {}", order.getId());

        try {
            Distributor distributor = (order.getDistributorId() != null)
                    ? distributorRepository.findById(order.getDistributorId()).orElse(null)
                    : null;

            String invoiceNumber = documentNumberService.generateDocumentNumber("DI");
            log.info("Generated dealer invoice number: {}", invoiceNumber);

            DealerInvoice invoiceEntity = new DealerInvoice();
            invoiceEntity.setInvoiceNumber(invoiceNumber);
            invoiceEntity.setDealerOrderId(order.getId());
            invoiceEntity.setDealerId(order.getDealerId());
            invoiceEntity.setDealerName(dealer.getFullName());
            invoiceEntity.setDistributorId(order.getDistributorId());
            invoiceEntity.setDistributorName(distributor != null
                    ? distributor.getFirstName() + " " + distributor.getLastName() : "");
            invoiceEntity.setTotalAmount(order.getAmount());
            invoiceEntity.setGrandTotal(order.getAmount());
            invoiceEntity.setInvoiceStatus(DealerInvoice.InvoiceStatus.GENERATED);
            dealerInvoiceRepository.save(invoiceEntity);

            DealerInvoiceDto dto = buildDto(order, dealer, distributor, invoiceNumber);
            String html = generateHtml(dto);
            byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);

            String pdfUrl = uploadToCloudinary(pdfBytes, invoiceNumber);
            invoiceEntity.setPdfUrl(pdfUrl);
            dealerInvoiceRepository.save(invoiceEntity);

            log.info("=== DEALER INVOICE GENERATION COMPLETED === Invoice: {}, URL: {}", invoiceNumber, pdfUrl);
            return pdfUrl;

        } catch (Exception e) {
            log.error("Dealer invoice generation failed for order {}: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Dealer invoice generation failed", e);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInvoiceByOrderId(Long dealerOrderId) {
        DealerInvoice invoice = dealerInvoiceRepository.findByDealerOrderId(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer invoice not found for order ID: " + dealerOrderId));
        return buildResponse(invoice);
    }

    @Transactional(readOnly = true)
    public byte[] downloadInvoicePdf(Long dealerOrderId) {
        log.info("=== DOWNLOAD DEALER INVOICE PDF === Order ID: {}", dealerOrderId);

        DealerInvoice invoiceEntity = dealerInvoiceRepository.findByDealerOrderId(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer invoice not found for order ID: " + dealerOrderId));

        if (invoiceEntity.getPdfUrl() == null || invoiceEntity.getPdfUrl().isEmpty()) {
            throw new RuntimeException("PDF not yet generated for dealer invoice: " + invoiceEntity.getInvoiceNumber());
        }

        try {
            return cloudinaryStorageService.downloadPdfByUrl(invoiceEntity.getPdfUrl());
        } catch (Exception e) {
            log.error("Failed to download dealer invoice PDF from Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to download dealer invoice PDF", e);
        }
    }

    @Transactional
    public void regeneratePdf(Long dealerOrderId) {
        log.info("=== DEALER INVOICE PDF REGENERATION STARTED === Order ID: {}", dealerOrderId);

        DealerOrder order = dealerOrderRepository.findById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("DealerOrder not found: " + dealerOrderId));

        java.util.Optional<DealerInvoice> invoiceOpt = dealerInvoiceRepository.findByDealerOrderId(dealerOrderId);

        if (invoiceOpt.isEmpty()) {
            // Invoice was never created (generation failed silently at order time) — create it now
            log.info("No dealer invoice found for order {} — generating from scratch", dealerOrderId);
            Dealer dealer = dealerRepository.findById(order.getDealerId())
                    .orElseThrow(() -> new RuntimeException("Dealer not found: " + order.getDealerId()));
            generateInvoice(order, dealer);
            return;
        }

        DealerInvoice invoiceEntity = invoiceOpt.get();
        Dealer dealer = dealerRepository.findById(invoiceEntity.getDealerId())
                .orElseThrow(() -> new RuntimeException("Dealer not found: " + invoiceEntity.getDealerId()));
        Distributor distributor = (invoiceEntity.getDistributorId() != null)
                ? distributorRepository.findById(invoiceEntity.getDistributorId()).orElse(null)
                : null;

        try {
            DealerInvoiceDto dto = buildDto(order, dealer, distributor, invoiceEntity.getInvoiceNumber());
            String html = generateHtml(dto);
            byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
            String newUrl = uploadToCloudinary(pdfBytes, invoiceEntity.getInvoiceNumber());
            invoiceEntity.setPdfUrl(newUrl);
            dealerInvoiceRepository.save(invoiceEntity);
            log.info("=== DEALER INVOICE PDF REGENERATION COMPLETED === Invoice: {}", invoiceEntity.getInvoiceNumber());
        } catch (Exception e) {
            log.error("Dealer invoice PDF regeneration failed for order {}: {}", dealerOrderId, e.getMessage(), e);
            throw new RuntimeException("Dealer invoice PDF regeneration failed", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInvoicesByDistributorId(Long distributorId) {
        return dealerInvoiceRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId)
                .stream().map(this::buildResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInvoicesByDealerId(Long dealerId) {
        return dealerInvoiceRepository.findByDealerIdOrderByCreatedAtDesc(dealerId)
                .stream().map(this::buildResponse).toList();
    }

    private DealerInvoiceDto buildDto(DealerOrder order, Dealer dealer, Distributor distributor, String invoiceNumber) {
        DealerInvoiceDto dto = new DealerInvoiceDto();
        dto.setInvoiceNumber(invoiceNumber);
        dto.setInvoiceDate(LocalDate.now());
        dto.setOrderNo(String.valueOf(order.getId()));
        dto.setOrderDate(order.getDate() != null
                ? order.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dto.setPaymentTerms("Due on Receipt");

        // Seller = Distributor (may be null if distributor_id was not set on the order)
        dto.setSellerName(distributor != null ? distributor.getFirstName() + " " + distributor.getLastName() : "");
        dto.setSellerAddress(distributor != null && distributor.getAddress() != null ? distributor.getAddress() : "");
        dto.setSellerPhone(distributor != null && distributor.getPhoneNumber() != null ? distributor.getPhoneNumber() : "");
        dto.setSellerGstin(distributor != null && distributor.getGstNumber() != null ? distributor.getGstNumber() : "");
        dto.setSellerState(distributor != null && distributor.getState() != null ? distributor.getState() : "");

        // Buyer = Dealer
        dto.setBuyerName(dealer.getFullName() != null ? dealer.getFullName() : "");
        dto.setBuyerAddress(dealer.getAddress() != null ? dealer.getAddress() : "");
        dto.setBuyerPhone(dealer.getPhone() != null ? dealer.getPhone() : "");

        // Build single item from order
        double total = order.getAmount() != null ? order.getAmount().doubleValue() : 0.0;
        int qty = order.getQuantity() != null ? order.getQuantity() : 1;
        double rate = qty > 0 ? total / qty : total;

        DealerInvoiceDto.Item item = new DealerInvoiceDto.Item();
        item.setSrNo(1);
        item.setSku(order.getSku() != null ? order.getSku() : "");
        item.setDescription(order.getItemName() != null ? order.getItemName() : order.getSku());
        item.setQuantity(qty);
        item.setRatePerUnit(Math.round(rate * 100.0) / 100.0);
        item.setAmount(total);

        dto.setItems(List.of(item));
        dto.setSubtotal(total);
        dto.setGrandTotal(total);
        dto.setTotalQty(String.valueOf(qty));
        dto.setAmountInWords(com.nector.userservice.util.NumberToWordsUtil.convertToWords(total));

        return dto;
    }

    private String generateHtml(DealerInvoiceDto dto) {
        Context context = new Context();
        context.setVariable("invoice", dto);
        context.setVariable("logoPath", htmlToPdfService.getDealerInvoiceLogoDataUri());
        return templateEngine.process("dealer-invoice", context);
    }

    private String uploadToCloudinary(byte[] pdfBytes, String invoiceNumber) throws IOException {
        Path tempDir = Path.of("temp");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        String fileName = invoiceNumber.replace("/", "-") + "_" + System.currentTimeMillis() + ".pdf";
        File tempFile = tempDir.resolve(fileName).toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(pdfBytes);
        }
        try {
            return cloudinaryStorageService.uploadPdf(tempFile);
        } finally {
            try { Files.deleteIfExists(tempFile.toPath()); } catch (IOException ignored) {}
        }
    }

    private Map<String, Object> buildResponse(DealerInvoice invoice) {
        return Map.ofEntries(
                Map.entry("id", invoice.getId()),
                Map.entry("invoiceNumber", invoice.getInvoiceNumber()),
                Map.entry("dealerOrderId", invoice.getDealerOrderId()),
                Map.entry("dealerId", invoice.getDealerId()),
                Map.entry("dealerName", invoice.getDealerName() != null ? invoice.getDealerName() : ""),
                Map.entry("distributorId", invoice.getDistributorId()),
                Map.entry("distributorName", invoice.getDistributorName() != null ? invoice.getDistributorName() : ""),
                Map.entry("totalAmount", invoice.getTotalAmount()),
                Map.entry("grandTotal", invoice.getGrandTotal()),
                Map.entry("invoiceStatus", invoice.getInvoiceStatus().toString()),
                Map.entry("pdfUrl", invoice.getPdfUrl() != null ? invoice.getPdfUrl() : ""),
                Map.entry("hasPdf", invoice.getPdfUrl() != null && !invoice.getPdfUrl().isEmpty()),
                Map.entry("createdAt", invoice.getCreatedAt())
        );
    }
}
