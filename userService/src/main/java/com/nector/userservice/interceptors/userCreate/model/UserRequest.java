package com.nector.userservice.interceptors.userCreate.model;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.enums.UserOnboardingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "User registration request")
public class UserRequest {

    @NotNull(message = "Onboarding type is required")
    @Schema(description = "USER or SALES", example = "USER")
    private UserOnboardingType userOnboardingType;

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Schema(description = "Unique username", example = "johndoe123")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User password", example = "password123")
    private String password;

    @NotNull(message = "Status is required")
    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus status;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Schema(description = "User first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "User last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Contact number must be a valid 10-digit Indian mobile number")
    @Schema(description = "Contact number", example = "9876543210")
    private String contactNo;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Alternate contact number must be a valid 10-digit Indian mobile number")
    @Schema(description = "Alternate Contact number", example = "9876543211")
    private String alternateContactNo;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-")
    @Schema(description = "Blood group of the user", example = "O+")
    private String bloodGroup;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    @Schema(description = "Full Address", example = "Detailed Address")
    private String completeAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "Patna")
    private String city;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "User's date of birth", example = "1995-08-15")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(?i)(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    @Schema(description = "User gender", example = "Male")
    private String gender;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Country", example = "India")
    private String country;

    @Pattern(regexp = "^\\d{5,10}$", message = "ZIP code must be 5-10 digits")
    @Schema(description = "ZIP code", example = "800001")
    private String zip;

    @Size(max = 50, message = "Employee roll number must not exceed 50 characters")
    @Schema(description = "Employee roll number", example = "EMP001")
    private String employeeRollNo;

    // --- USER type fields ---

    @Schema(description = "Required when userOnboardingType=USER. E.g. ACCOUNT_MGR", example = "ACCOUNT_MGR")
    private String roleType;

    // --- SALES type fields ---

    @Schema(description = "Required when userOnboardingType=SALES", example = "AREA_SALES_MGR")
    private SalesRole salesRole;

    @Size(max = 100)
    @Schema(description = "Zone (SALES only)", example = "BIHAR")
    private String zone;

    @Size(max = 100)
    @Schema(description = "Region (SALES only)", example = "BIHAR")
    private String region;
}
