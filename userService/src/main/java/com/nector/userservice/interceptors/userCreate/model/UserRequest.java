package com.nector.userservice.interceptors.userCreate.model;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

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
    @Schema(description = "User password", example = "SecurePass123!")
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
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City", example = "New York")
    private String city;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Country", example = "USA")
    private String country;
    
    @Pattern(regexp = "^\\d{5,10}$", message = "ZIP code must be 5-10 digits")
    @Schema(description = "ZIP code", example = "10001")
    private String zip;
    
    @NotNull(message = "Role type is required")
    @Schema(description = "User role", example = "USER")
    private RoleType roleType;
}