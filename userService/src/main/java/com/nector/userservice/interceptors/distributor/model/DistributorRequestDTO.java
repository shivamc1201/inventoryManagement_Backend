package com.nector.userservice.interceptors.distributor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Distributor creation/update request")
public class DistributorRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String name; // Distributor Name

    @NotBlank
    @Size(max = 100)
    private String assignedPerson;

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

    @NotBlank
    @Pattern(regexp = "^[0-9]{12}$", message = "Invalid Aadhaar number")
    private String aadhaarNumber;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number"
    )
    private String panNumber;

    @NotBlank
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST number"
    )
    private String gstNumber;

    @NotNull
    private DistributorStatus status;
}
