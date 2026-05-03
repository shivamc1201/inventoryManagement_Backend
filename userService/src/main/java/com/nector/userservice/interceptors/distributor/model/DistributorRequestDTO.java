package com.nector.userservice.interceptors.distributor.model;

import com.nector.userservice.enums.SalesRole;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nector.userservice.util.BigDecimalDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Distributor creation/update request")
public class DistributorRequestDTO {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "First name", example = "John")
    private String firmName;

    @NotBlank
    @Size(max = 100)
    private String assignedPerson;


    private Long salespersonId;

    @Schema(description = "Sales person role type", example = "SALES_EXECUTIVE")
    private SalesRole salesPersonRoleType;

    @NotBlank
    private String distributorType;

    @NotBlank
    private String companyType;

    @NotBlank
    @Email
    private String contactEmail;

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$")
    private String phoneNumber;

    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$")
    private String alternateContact;

    @NotBlank
    @Size(max = 200)
    private String address;

    private String aadhaarNumber;

    private String panNumber;

    private String gstNumber;

    @NotNull
    private DistributorStatus status;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Schema(description = "Unique username", example = "johndoe123")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User password", example = "SecurePass123!")
    private String password;


    private String accountNumber;


    private String accountName;


    private String ifsc;

    @Schema(description = "Bank guarantee number", example = "BG123456789")
    private String bankGuaranteeNumber;

    @Schema(description = "Credit amount (limit value)", example = "100000")
    private BigDecimal creditAmount;

    @Schema(description = "Credit limit enabled (true/false)", example = "true")
    private Boolean creditLimit;

    @Schema(description = "Credit balance", example = "450000.00")
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private java.math.BigDecimal creditBalance;

    @Schema(description = "Bank guarantee expiry date", example = "2025-12-31")
    private java.time.LocalDate bgExpiryDate;

    @Schema(description = "District", example = "Patna")
    private String district;

    @Schema(description = "State", example = "Bihar")
    private String state;

    @Schema(description = "PIN code", example = "800001")
    private String pinCode;

    @Schema(description = "Key person", example = "John Doe")
    private String keyperson;

}
