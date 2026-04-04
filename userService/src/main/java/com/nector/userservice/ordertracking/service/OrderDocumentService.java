package com.nector.userservice.ordertracking.service;

import com.nector.userservice.ordertracking.entity.OrderDocument;
import com.nector.userservice.ordertracking.repository.OrderDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDocumentService {

    private final OrderDocumentRepository docRepo;

    public byte[] getDocument(Long orderId, String type) {
        OrderDocument doc = docRepo.findByOrderIdAndDocType(orderId, type)
            .orElseThrow(() -> new RuntimeException("Document not found for order " + orderId + " and type " + type));

        try {
            // Option A: Read from local filesystem
            return Files.readAllBytes(Paths.get(doc.getStoragePath()));

            // Option B: Read from AWS S3 (replace Option A with this if using S3)
            // return s3Client.getObjectAsBytes(
            //     GetObjectRequest.builder()
            //         .bucket("ims-documents")
            //         .key(doc.getStoragePath())
            //         .build()
            // ).asByteArray();
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
        OrderDocument doc = OrderDocument.builder()
            .docType(docType)
            .fileName(fileName)
            .storagePath(storagePath)
            .build();
        
        return docRepo.save(doc);
    }
}
