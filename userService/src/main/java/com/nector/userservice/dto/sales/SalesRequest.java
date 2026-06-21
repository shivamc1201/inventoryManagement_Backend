package com.nector.userservice.dto.sales;

import com.nector.userservice.model.PriorityLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;

    private String phoneNumber;

    @NotBlank(message = "Subject is required")
    private String subject;

    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;

    @NotBlank(message = "Description is required")
    private String description;

    // ✅ Added fields
    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Sale date is required")
    private LocalDateTime saleDate;
}
