package com.nector.userservice.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;
    private final CloudinaryConfig.CloudinaryProperties properties;

    /**
     * Uploads PDF file to Cloudinary with retry logic
     * @param file PDF file to upload
     * @return secure URL of uploaded file
     * @throws CloudinaryUploadException if upload fails after retries
     */



    @Retryable(
            value = {CloudinaryUploadException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String uploadPdf(File file) {
        log.info("=== CLOUDINARY PDF UPLOAD STARTED ===");
        log.info("Upload details - File: {}, Size: {} bytes, Timestamp: {}",
                file.getName(), file.length(), java.time.LocalDateTime.now());

        validateFile(file);
        log.info("File validation passed - PDF format, size within limits");

        String publicId = generatePublicId(file.getName());
        log.info("Generated Cloudinary public ID: {}", publicId);

        try {
            log.info("Starting Cloudinary upload attempt...");
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "public_id", "invoices/" + publicId,
                    "overwrite", true,
                    "invalidate", true
            );
            log.info("Upload parameters configured - Folder: invoices, Resource type: auto");

            log.info("Executing Cloudinary upload...");
            Map uploadResult = cloudinary.uploader().upload(file, uploadParams);
            String secureUrl = uploadResult.get("secure_url").toString();

            log.info("=== CLOUDINARY UPLOAD COMPLETED ===");
            log.info("Upload successful - File: {}, URL: {}, Size: {} bytes",
                    file.getName(), secureUrl, file.length());
            log.info("Cloudinary response - Public ID: {}, Secure URL: {}", publicId, secureUrl);

            return secureUrl;

        } catch (Exception e) {
            log.error("=== CLOUDINARY UPLOAD FAILED ===");
            log.error("Upload failure details - File: {}, Error: {}, Public ID: {}, Timestamp: {}",
                    file.getName(), e.getMessage(), publicId, java.time.LocalDateTime.now());
            log.error("Upload parameters used - Folder: invoices, Resource type: raw, Public ID: {}", publicId);
            log.error("Stack trace:", e);
            throw new CloudinaryUploadException("PDF upload failed: " + e.getMessage(), e);
        }
    }

    public byte[] downloadPdfByPublicIds(String publicId) {
        log.info("=== DOWNLOAD PDF BY PUBLIC ID ===");
        log.info("Download request - Public ID: {}, Timestamp: {}", publicId, java.time.LocalDateTime.now());

        try {
            // Generate signed URL with full path
            String signedUrl = cloudinary.url()
                    .resourceType("raw")
                    .publicId(publicId)
                    .signed(true)
                    .generate();

            log.info("Generated signed URL: {}", signedUrl);

            // Download file
            java.net.URL url = new java.net.URL(signedUrl);
            byte[] fileBytes;

            try (java.io.InputStream inputStream = url.openStream()) {
                fileBytes = inputStream.readAllBytes();
            }

            log.info("PDF downloaded successfully from Cloudinary - Size: {} bytes", fileBytes.length);
            return fileBytes;

        } catch (Exception e) {
            log.error("Failed to download PDF from Cloudinary - Public ID: {}, Error: {}", publicId, e.getMessage());
            throw new RuntimeException("Failed to download PDF from Cloudinary", e);
        }
    }



    /**
     * Uploads PDF from MultipartFile with retry logic
     * @param multipartFile MultipartFile containing PDF
     * @return secure URL of uploaded file
     * @throws CloudinaryUploadException if upload fails after retries
     */
    @Retryable(
            value = {CloudinaryUploadException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String uploadPdf(MultipartFile multipartFile) {
        validateMultipartFile(multipartFile);

        try {
            File tempFile = convertToTempFile(multipartFile);
            try {
                return uploadPdf(tempFile);
            } finally {
                cleanupTempFile(tempFile);
            }
        } catch (IOException e) {
            log.error("Failed to process multipart file: {}", e.getMessage());
            throw new CloudinaryUploadException("File processing failed", e);
        }
    }

    /**
     * Deletes file from Cloudinary
     * @param publicId Public ID of file to delete
     * @return true if deletion successful
     */
    public boolean deleteFile(String publicId) {
        try {
            Map<String, Object> deleteParams = ObjectUtils.asMap(
                    "resource_type", "raw",
                    "invalidate", true
            );

            Map result = cloudinary.uploader().destroy(publicId, deleteParams);
            boolean deleted = "ok".equals(result.get("result"));

            log.info("File deletion {}: {}", deleted ? "successful" : "failed", publicId);
            return deleted;

        } catch (Exception e) {
            log.error("Failed to delete file: {} - {}", publicId, e.getMessage());
            return false;
        }
    }

    private void validateFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (file.length() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private String generatePublicId(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String nameWithoutExt = originalFilename.replace(".pdf", "").replaceAll("[^a-zA-Z0-9]", "_");
        return String.format("invoice_%s_%s_%s", nameWithoutExt, timestamp, uuid);
    }

    private File convertToTempFile(MultipartFile multipartFile) throws IOException {
        String tempFileName = "temp_" + System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        File tempFile = Files.createTempFile("upload_", tempFileName).toFile();

        multipartFile.transferTo(tempFile);
        log.debug("Created temp file: {}", tempFile.getAbsolutePath());

        return tempFile;
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            try {
                Files.deleteIfExists(tempFile.toPath());
                log.debug("Cleaned up temp file: {}", tempFile.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to cleanup temp file: {} - {}", tempFile.getAbsolutePath(), e.getMessage());
            }
        }
    }

    public String generateSignedUrl(String publicId) {
        try {
            // Create signed URL with expiration
            return cloudinary.url()
                    .resourceType("raw")
                    .publicId(publicId)
                    .format("pdf")
                    .signed(true)
                    .generate();
        } catch (Exception e) {
            log.error("Failed to generate signed URL: {}", e.getMessage());
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    public byte[] downloadPdfFromCloudinary(String publicId) {
        try {
            // Generate signed URL for download
            String signedUrl = cloudinary.url()
                    .resourceType("raw")
                    .publicId(publicId)
                    .format("pdf")
                    .signed(true)
                    .generate();

            log.info("Generated download URL: {}", signedUrl);

            // Download the file
            java.net.URL url = new java.net.URL(signedUrl);
            byte[] fileBytes;

            try (java.io.InputStream inputStream = url.openStream()) {
                fileBytes = inputStream.readAllBytes();
            }

            log.info("PDF downloaded successfully from Cloudinary - Size: {} bytes", fileBytes.length);
            return fileBytes;

        } catch (Exception e) {
            log.error("Failed to download PDF from Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to download PDF", e);
        }
    }


    public byte[] downloadPdfByPublicId(String publicId) {

        try {

            log.info("Downloading from Cloudinary publicId: {}", publicId);

            Map<String,Object> result =
                    cloudinary.api().resource(
                            publicId,
                            ObjectUtils.asMap(
                                    "resource_type","raw",
                                    "type","upload"   // ⭐ THIS FIXES IT
                            )
                    );

            String downloadUrl =
                    result.get("secure_url").toString();

            log.info("Cloudinary download URL: {}", downloadUrl);

            try(InputStream is =
                        new URL(downloadUrl).openStream()){

                return is.readAllBytes();

            }

        } catch(Exception e){

            log.error("Cloudinary download failed",e);

            throw new RuntimeException(
                    "Failed to download PDF",e);

        }
    }


    public byte[] downloadPdfByUrl(String pdfUrl) {

        try {

            log.info("Downloading PDF from URL: {}", pdfUrl);

            try (InputStream is =
                         new URL(pdfUrl).openStream()) {

                byte[] data = is.readAllBytes();

                log.info("PDF downloaded successfully size: {}", data.length);

                return data;
            }

        } catch (Exception e) {

            log.error("PDF download failed", e);

            throw new RuntimeException(
                    "Failed to download PDF", e);
        }
    }
}