package com.nector.userservice.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nector.userservice.cloudinary.CloudinaryConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.util.Map;

@Configuration
@EnableRetry
@Slf4j
public class CloudinaryConfig {

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating Cloudinary configuration...");

        // Validate using constants
        if (CloudinaryConstants.CLOUD_NAME == null || CloudinaryConstants.CLOUD_NAME.trim().isEmpty()) {
            throw new IllegalStateException(CloudinaryConstants.ERROR_CLOUD_NAME_MISSING);
        }
        if (CloudinaryConstants.API_KEY == null || CloudinaryConstants.API_KEY.trim().isEmpty()) {
            throw new IllegalStateException(CloudinaryConstants.ERROR_API_KEY_MISSING);
        }
        if (CloudinaryConstants.API_SECRET == null || CloudinaryConstants.API_SECRET.trim().isEmpty()) {
            throw new IllegalStateException(CloudinaryConstants.ERROR_API_SECRET_MISSING);
        }

        log.info("Cloudinary configuration validated successfully for cloud: {}", CloudinaryConstants.CLOUD_NAME);
    }

    @Bean
    public Cloudinary cloudinary() {
        log.info("Initializing Cloudinary client for cloud: {}", CloudinaryConstants.CLOUD_NAME);

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CloudinaryConstants.CLOUD_NAME,
                "api_key", CloudinaryConstants.API_KEY,
                "api_secret", CloudinaryConstants.API_SECRET,
                "timeout", CloudinaryConstants.DEFAULT_UPLOAD_TIMEOUT,
                "secure", true
        ));

        log.info("Cloudinary client initialized successfully");
        return cloudinary;
    }

    @Bean
    public CloudinaryProperties cloudinaryProperties() {
        return new CloudinaryProperties(
                CloudinaryConstants.DEFAULT_UPLOAD_RETRIES,
                CloudinaryConstants.DEFAULT_UPLOAD_TIMEOUT
        );
    }

    public static class CloudinaryProperties {
        private final int maxRetries;
        private final int uploadTimeout;

        public CloudinaryProperties(int maxRetries, int uploadTimeout) {
            this.maxRetries = maxRetries;
            this.uploadTimeout = uploadTimeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public int getUploadTimeout() {
            return uploadTimeout;
        }
    }
}