package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawProductRequest {

    private String name;

    private String materialCode;

    private Unit unit;

    private BigDecimal price;

    private BigDecimal quantity;

    private BigDecimal minimumThreshold;

    private String hsn;

    private BigDecimal taxRate;

    private String vendorId;

    private String vendorName;

    private String transportName;

    private String driverName;

    private String driverMobile;

    private ProductStatus status;

    private BigDecimal rate;

    private BigDecimal gst;

    private BigDecimal grossAmount;
}