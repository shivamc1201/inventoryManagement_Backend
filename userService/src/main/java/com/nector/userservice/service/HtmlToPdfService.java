package com.nector.userservice.service;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@Service
public class HtmlToPdfService {

    public byte[] convertHtmlToPdf(String html) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert HTML to PDF", e);
        }
    }

    public String getLogoDataUri() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("static/Nectar Logo.png")) {
            if (is == null) throw new RuntimeException("Logo not found in classpath: static/Nectar Logo.png");
            byte[] bytes = is.readAllBytes();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load logo", e);
        }
    }
}
