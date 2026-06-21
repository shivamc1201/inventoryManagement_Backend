package com.nector.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class HtmlToPdfService {

    @Autowired
    private ResourceLoader resourceLoader;

    private volatile String logoUri;
    private volatile String emailIconUri;
    private volatile String webIconUri;

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

    // Kept original name so all existing callers (InvoiceService, ProformaInvoiceService, GdnService) work unchanged.
    // Now returns a file:// URI that Flying Saucer can actually load — data: URIs fail silently in iText 2.1.7.
    public String getLogoDataUri() {
        if (logoUri == null) {
            synchronized (this) {
                if (logoUri == null) {
                    logoUri = loadResourceToTempFile("classpath:static/nectar-logo.png", "nectar-logo", ".png");
                }
            }
        }
        return logoUri;
    }

    public String getEmailIconUri() {
        if (emailIconUri == null) {
            synchronized (this) {
                if (emailIconUri == null) {
                    emailIconUri = writeBytesToTempFile(buildEmailIconBytes(), "email-icon", ".png");
                }
            }
        }
        return emailIconUri;
    }

    public String getWebIconUri() {
        if (webIconUri == null) {
            synchronized (this) {
                if (webIconUri == null) {
                    webIconUri = writeBytesToTempFile(buildWebIconBytes(), "web-icon", ".png");
                }
            }
        }
        return webIconUri;
    }

    private String loadResourceToTempFile(String classpathLocation, String prefix, String suffix) {
        try {
            Resource resource = resourceLoader.getResource(classpathLocation);
            byte[] bytes;
            try (InputStream is = resource.getInputStream()) {
                bytes = is.readAllBytes();
            }
            return writeBytesToTempFile(bytes, prefix, suffix);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + classpathLocation, e);
        }
    }

    private String writeBytesToTempFile(byte[] bytes, String prefix, String suffix) {
        try {
            Path temp = Files.createTempFile(prefix + "-", suffix);
            temp.toFile().deleteOnExit();
            Files.write(temp, bytes);
            return temp.toUri().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write temp file: " + prefix, e);
        }
    }

    // Draws a simple envelope rectangle with a V-fold line (14x10 px)
    private byte[] buildEmailIconBytes() {
        try {
            int w = 14, h = 10;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(51, 51, 51));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRect(0, 0, w - 1, h - 1);
            g.drawLine(0, 0, w / 2 - 1, h / 2);
            g.drawLine(w - 1, 0, w / 2, h / 2);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate email icon", e);
        }
    }

    // Draws a simple globe: circle + horizontal + vertical lines (12x12 px)
    private byte[] buildWebIconBytes() {
        try {
            int w = 12, h = 12;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(51, 51, 51));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(0, 0, w - 1, h - 1);
            g.drawLine(0, (h - 1) / 2, w - 1, (h - 1) / 2);
            g.drawLine((w - 1) / 2, 0, (w - 1) / 2, h - 1);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate web icon", e);
        }
    }
}
