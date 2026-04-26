package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.ScrapItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScrapItemRequest {
    
    private String name;
    
    private String itemCode;
    
    private ScrapItem.Unit unit;
    
    private BigDecimal price;
    
    private Integer quantity;
    
    private Integer minimumThreshold;

    private String vendorId;

    private String vendorName;

    private String transportName;

    private String driverName;

    private String driverMobile;
}
