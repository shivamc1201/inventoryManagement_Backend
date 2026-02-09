package com.nector.userservice.aws;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ImageService {

    private static final String BUCKET_NAME = "peoject-nector-images";
    private static final String CLOUDFRONT_URL = "https://dxxxxxxxx.cloudfront.net";

    private final S3Client s3Client;

    public S3ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadProductImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String key = "product-images/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentType(file.getContentType())
                .cacheControl("public, max-age=31536000")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );

        return CLOUDFRONT_URL + "/" + key;
    }
}

