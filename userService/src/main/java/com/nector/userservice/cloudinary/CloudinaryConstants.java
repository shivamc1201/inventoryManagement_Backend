package com.nector.userservice.cloudinary;

public final class CloudinaryConstants {

    // Prevent instantiation
    private CloudinaryConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String CLOUD_NAME = "dofswbzgs";
    public static final String API_KEY = "368277218995857";
    public static final String API_SECRET = "YCTPEr8XirXYGBCFHkiCeZ6oZns";

    // Default configuration values
    public static final int DEFAULT_UPLOAD_TIMEOUT = 30000;
    public static final int DEFAULT_UPLOAD_RETRIES = 3;

    // Upload configuration
    public static final String RESOURCE_TYPE_AUTO = "auto";
    public static final String RESOURCE_TYPE_RAW = "raw";
    public static final String FOLDER_INVOICES = "invoices";

    // Error messages
    public static final String ERROR_CLOUD_NAME_MISSING = "Cloudinary cloud name is not configured";
    public static final String ERROR_API_KEY_MISSING = "Cloudinary API key is not configured";
    public static final String ERROR_API_SECRET_MISSING = "Cloudinary API secret is not configured";
}