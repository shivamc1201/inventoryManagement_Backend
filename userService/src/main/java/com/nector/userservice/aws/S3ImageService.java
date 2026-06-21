package com.nector.userservice.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ImageService {

    private static final Logger log = LoggerFactory.getLogger(S3ImageService.class);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;
    
    @Value("${aws.account-id}")
    private String accountId;

    private final S3Client s3Client;

    public S3ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
        log.info("S3ImageService initialized");
    }

    public String uploadProductImage(MultipartFile file) throws IOException {
        log.info("Starting image upload - bucket: {}, accountId: {}", bucketName, accountId);
        
        if (file == null || file.isEmpty()) {
            log.error("Image validation failed: file is null or empty");
            throw new IllegalArgumentException("Image is required");
        }

        String contentType = file.getContentType();
        log.info("File details - name: {}, size: {}, contentType: {}", 
                file.getOriginalFilename(), file.getSize(), contentType);
        
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("Content type validation failed: {}", contentType);
            throw new IllegalArgumentException("Only image files allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "-" + (originalFilename != null ? originalFilename : "image");
        String key = "product-images/" + fileName;
        log.info("Generated S3 key: {}", key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000")
                    .expectedBucketOwner(accountId)
                    .build();

            log.info("Uploading to S3...");
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            
            String imageUrl = cloudfrontUrl + "/" + key;
            log.info("Upload successful - URL: {}", imageUrl);
            return imageUrl;
        } catch (S3Exception e) {
            log.error("S3Exception during upload - Code: {}, Message: {}", e.awsErrorDetails().errorCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (SdkClientException e) {
            log.error("SdkClientException during upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }
}

