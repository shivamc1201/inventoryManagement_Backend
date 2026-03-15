package com.nector.userservice.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;


@Configuration
@EnableRetry
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Value("${cloudinary.upload.timeout:30000}")
    private int uploadTimeout;

    @Value("${cloudinary.upload.retries:3}")
    private int maxRetries;

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating Cloudinary configuration...");

        if (cloudName == null || cloudName.trim().isEmpty()) {
            throw new IllegalStateException("Cloudinary cloud name is not configured");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Cloudinary API key is not configured");
        }
        if (apiSecret == null || apiSecret.trim().isEmpty()) {
            throw new IllegalStateException("Cloudinary API secret is not configured");
        }

        log.info("Cloudinary configuration validated successfully for cloud: {}", cloudName);
    }

    @Bean
    public Cloudinary cloudinary() {
        log.info("Initializing Cloudinary client for cloud: {}", cloudName);

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "timeout", uploadTimeout,
                "secure", true
        ));

        log.info("Cloudinary client initialized successfully");
        return cloudinary;
    }

    @Bean
    public CloudinaryProperties cloudinaryProperties() {
        return new CloudinaryProperties(maxRetries, uploadTimeout);
    }

    public static class CloudinaryProperties {
        private final int maxRetries;
        private final int uploadTimeout;

        public CloudinaryProperties(int maxRetries, int uploadTimeout) {
            this.maxRetries = maxRetries;
            this.uploadTimeout = uploadTimeout;
        }

        public int getMaxRetries() { return maxRetries; }
        public int getUploadTimeout() { return uploadTimeout; }
    }
}