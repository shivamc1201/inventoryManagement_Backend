package com.nector.userservice.cloudinary;

public class PdfGenerationResult {
    boolean success;
    String message;
    String pdfUrl;

    public static PdfGenerationResult success(String pdfUrl, long pdfSize) {
        PdfGenerationResult result = new PdfGenerationResult();
        result.success = true;
        result.pdfUrl = pdfUrl;
        result.message = "PDF generated successfully. Size: " + pdfSize + " bytes";
        return result;
    }

    public static PdfGenerationResult failure(String message) {
        PdfGenerationResult result = new PdfGenerationResult();
        result.success = false;
        result.message = message;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }
}
