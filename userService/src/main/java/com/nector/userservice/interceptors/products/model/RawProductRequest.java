package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.enums.ProductStatus;
import com.nector.userservice.enums.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawProductRequest {

    public interface OnCreate {}
    public interface OnInward {}

    @NotBlank(groups = OnCreate.class, message = "Item name is required")
    private String name;

    @NotBlank(groups = OnCreate.class, message = "Material code is required")
    private String materialCode;

    @NotNull(groups = OnCreate.class, message = "Unit is required")
    private Unit unit;

    @NotNull(groups = OnInward.class, message = "Price is required")
    @Positive(groups = OnInward.class, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(groups = OnInward.class, message = "Quantity is required")
    @Positive(groups = OnInward.class, message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private BigDecimal minimumThreshold;

    private String hsn;

    private BigDecimal taxRate;

    private String vendorId;

    @NotBlank(groups = OnInward.class, message = "Vendor name is required")
    private String vendorName;

    private String transportName;

    private String driverName;

    private String driverMobile;

    private ProductStatus status;

    private BigDecimal rate;

    private BigDecimal gst;

    private BigDecimal grossAmount;

    @NotBlank(groups = OnInward.class, message = "Voucher number is required")
    private String voucherNo;
}
