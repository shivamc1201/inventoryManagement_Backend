package com.nector.userservice.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
@Data
@Setter
public class UserUpdateRequest {

    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters")
    @Schema(description = "Unique username", example = "johndoe123")
    private String username;

    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus status;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Schema(description = "User first name", example = "John")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "User last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Contact number must be valid")
    @Schema(description = "Contact number", example = "+1234567890")
    private String contactNo;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Alternate Contact number must be valid")
    @Schema(description = "Alternate Contact number", example = "+1234567890")
    private String alternateContactNo;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be valid")
    @Schema(description = "Blood group", example = "O+")
    private String bloodGroup;

    @Size(max = 100, message = "Address must not exceed 100 characters")
    private String completeAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(?i)(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Size(max = 100)
    private String country;

    @Pattern(regexp = "^\\d{5,10}$", message = "ZIP code must be 5-10 digits")
    private String zip;

    @Schema(description = "Role type", example = "ACCOUNT_MGR")
    private RoleType roleType;
}
