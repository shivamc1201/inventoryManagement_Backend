package com.nector.userservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerCreateRequest {

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be a valid 10-digit number")
    private String phone;

    @Size(max = 300, message = "Address cannot exceed 300 characters")
    private String address;

    private BigDecimal openingBalance;

}
