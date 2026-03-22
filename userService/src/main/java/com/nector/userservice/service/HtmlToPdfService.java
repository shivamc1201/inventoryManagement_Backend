package com.nector.userservice.service;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class HtmlToPdfService {
    
//    public byte[] convertHtmlToPdf(String html) {
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ITextRenderer renderer = new ITextRenderer();
//            renderer.setDocumentFromString(html);
//            renderer.layout();
//            renderer.createPDF(outputStream);
//            return outputStream.toByteArray();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to convert HTML to PDF", e);
//        }
//    }

    public byte[] convertHtmlToPdf(String html) {

        try {

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(
                    html,
                    new File("src/main/resources/templates").toURI().toURL().toString()
            );

            renderer.layout();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            renderer.createPDF(out);

            return out.toByteArray();

        } catch(Exception e){

            throw new RuntimeException("Failed to convert HTML to PDF",e);

        }

    }
}