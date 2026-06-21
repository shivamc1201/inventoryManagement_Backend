package com.nector.userservice.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    Cloudinary cloudinary;

    public String uploadPdf(File file) {

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file,
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "invoices"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("PDF upload failed", e);
        }
    }
    
    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "finished-products"
                    )
            );
            
            return uploadResult.get("secure_url").toString();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file", e);
        } catch (Exception e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }
}
