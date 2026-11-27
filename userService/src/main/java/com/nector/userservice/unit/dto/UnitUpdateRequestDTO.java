package com.nector.userservice.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payload for PUT /api/units/{id}.
 *
 * A "unit" is stored as a row in either raw_products or finished_products, and ids are NOT
 * unique across the two tables, so {@code category} is required to pick the right one.
 * Every other field is optional: only the fields that are present are changed.
 */
@Data
public class UnitUpdateRequestDTO {

    @NotBlank(message = "Category is required")
    private String category;

    // ---- Raw Material fields ----
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String unitName;

    @Size(max = 50, message = "Unit code must not exceed 50 characters")
    private String unitCode;

    @Size(max = 50, message = "Unit type must not exceed 50 characters")
    private String unitType;

    @Size(max = 50, message = "Product size must not exceed 50 characters")
    private String productSize;

    // ---- Finished Product fields ----
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    private BigDecimal price;

    private Integer quantity;

    private Integer minimumThreshold;

    // ---- Common ----
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** ACTIVE / INACTIVE. Stored as the unit status. */
    private String status;
}
