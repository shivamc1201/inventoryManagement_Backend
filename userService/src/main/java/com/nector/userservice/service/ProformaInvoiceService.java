package com.nector.userservice.service;

import com.nector.userservice.dto.invoice.InvoiceItem;
import com.nector.userservice.dto.invoice.ProformaInvoice;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProformaInvoiceService {
    
    private final CartRepository cartRepository;
    private final TemplateEngine templateEngine;
    private final HtmlToPdfService htmlToPdfService;
    
    public void generateProformaInvoice(Long cartId) {
        log.info("Generating proforma invoice for cart ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
            
        ProformaInvoice invoice = createInvoiceFromCart(cart);
        String html = generateHtmlFromTemplate(invoice);
        byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);
        
        saveInvoiceToFile(pdfBytes, invoice.getPiNumber());
        log.info("Proforma invoice generated successfully: {}", invoice.getPiNumber());
    }
    
    private ProformaInvoice createInvoiceFromCart(Cart cart) {
        ProformaInvoice invoice = new ProformaInvoice();
        
        // Invoice details
        invoice.setPiNumber("PI-" + cart.getId() + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        invoice.setPiDate(LocalDate.now());
        invoice.setModeOfPayment("Bank Transfer");
        
        // Seller details
        invoice.setCompanyName("Your Company Name");
        invoice.setCompanyAddress("Your Company Address");
        invoice.setGstin("Your GSTIN");
        invoice.setContactNumber("+91-XXXXXXXXXX");
        invoice.setEmail("sales@yourcompany.com");
        
        // Items from cart
        List<InvoiceItem> items = IntStream.range(0, cart.getCartItems().size())
            .mapToObj(i -> {
                CartItem cartItem = cart.getCartItems().get(i);
                InvoiceItem item = new InvoiceItem();
                item.setSrNo(i + 1);
                item.setDescription(cartItem.getItem().getName());
                item.setHsnCode("1234"); // Default HSN
                item.setQuantity(cartItem.getQuantity());
                item.setRatePerUnit(cartItem.getPriceAtTime().doubleValue());
                item.setUnit("Pcs");
                item.setAmount(cartItem.getPriceAtTime().doubleValue() * cartItem.getQuantity());
                return item;
            })
            .toList();
        
        invoice.setItems(items);
        
        // Calculate totals
        double subtotal = items.stream().mapToDouble(InvoiceItem::getAmount).sum();
        double cgst = subtotal * 0.09; // 9%
        double sgst = subtotal * 0.09; // 9%
        double grandTotal = subtotal + cgst + sgst;
        
        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(0.0);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountInWords(convertToWords(grandTotal));
        
        return invoice;
    }
    
    private String generateHtmlFromTemplate(ProformaInvoice invoice) {
        Context context = new Context();
        context.setVariable("invoice", invoice);
        return templateEngine.process("proforma-invoice", context);
    }
    
    private void saveInvoiceToFile(byte[] pdfBytes, String piNumber) {
        try {
            File dir = new File("invoices");
            if (!dir.exists()) dir.mkdirs();
            
            String filePath = "invoices/" + piNumber + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
            log.info("PDF saved to: {}", new File(filePath).getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to save invoice", e);
        }
    }
    
    private String convertToWords(double amount) {
        return String.format("%.0f Rupees Only", amount);
    }
}