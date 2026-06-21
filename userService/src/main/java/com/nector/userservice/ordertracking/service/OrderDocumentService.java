package com.nector.userservice.ordertracking.service;

import com.nector.userservice.ordertracking.entity.OrderDocument;
import com.nector.userservice.ordertracking.entity.OrderTracking;
import com.nector.userservice.ordertracking.repository.OrderDocumentRepository;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDocumentService {

    private final OrderDocumentRepository docRepo;
    private final OrderTrackingRepository orderRepo;

    public byte[] getDocument(Long orderId, String type) {
        OrderDocument doc = docRepo.findByOrderIdAndDocType(orderId, type)
            .orElseThrow(() -> new RuntimeException("Document not found for order " + orderId + " and type " + type));

        String path = doc.getStoragePath();
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                try (InputStream in = new URL(path).openStream()) {
                    return in.readAllBytes();
                }
            }
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            log.error("Failed to read document: {}", e.getMessage());
            throw new RuntimeException("Failed to read document", e);
        }
    }

    public String getFilename(Long orderId, String type) {
        return docRepo.findByOrderIdAndDocType(orderId, type)
            .map(OrderDocument::getFileName)
            .orElse(type + "-" + orderId + ".pdf");
    }

    public Resource getResource(Long orderId, String type) {
        byte[] data = getDocument(orderId, type);
        String filename = getFilename(orderId, type);
        return new ByteArrayResource(data, filename);
    }

    /**
     * Store document reference in database
     */
    public OrderDocument saveDocument(Long orderId, String docType, String fileName, String storagePath) {
        OrderTracking order = orderRepo.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("OrderTracking not found: " + orderId));

        // Upsert: replace existing doc of same type for the same order
        docRepo.findByOrderIdAndDocType(orderId, docType).ifPresent(docRepo::delete);

        OrderDocument doc = OrderDocument.builder()
            .order(order)
            .docType(docType)
            .fileName(fileName)
            .storagePath(storagePath)
            .build();

        return docRepo.save(doc);
    }
}
