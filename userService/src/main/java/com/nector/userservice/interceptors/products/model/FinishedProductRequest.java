package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class FinishedProductRequest {
    
    private String name;
    
    private String description;
    
    private String sku;
    
    private Unit unit;
    
    private BigDecimal weight;
    
    private BigDecimal price;
    
    private Integer quantity;
    
    private Integer minimumThreshold;
    
    private String hsn;
    
    private BigDecimal taxRate;
    
    private Boolean active;

    private ProductStatus status;

    private BigDecimal rate;

    private BigDecimal gst;

    private BigDecimal grossAmount;
}
