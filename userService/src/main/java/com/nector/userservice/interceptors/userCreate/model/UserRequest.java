package com.nector.userservice.interceptors.userCreate.model;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "User registration request")
public class UserRequest {
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
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Contact number must be valid")
    @Schema(description = "Contact number", example = "+1234567890")
    private String contactNo;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Alternate Contact number must be valid")
    @Schema(description = "Alternate Contact number", example = "+1234567890")
    private String alternateContactNo;

    @NotBlank(message = "Blood group is required")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-")
    @Schema(description = "Blood group of the user", example = "O+")
    private String bloodGroup;

    @Size(max = 100, message = "Address must not exceed 100 characters")
    @Schema(description = "Full Address", example = "Detailed Address")
    private String completeAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "New York")
    private String city;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "User's date of birth", example = "1995-08-15")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(?i)(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    @Schema(description = "User gender", example = "Male")
    private String gender;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Country", example = "USA")
    private String country;
    
    @Pattern(regexp = "^\\d{5,10}$", message = "ZIP code must be 5-10 digits")
    @Schema(description = "ZIP code", example = "10001")
    private String zip;
    
    @NotNull(message = "Role type is required")
    @Schema(description = "ACCOUNT_MGR", example = "ACCOUNT_MGR")
    private RoleType roleType;
}