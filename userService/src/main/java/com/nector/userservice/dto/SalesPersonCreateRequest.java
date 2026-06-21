package com.nector.userservice.dto;

import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.common.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesPersonCreateRequest {

    // Basic salesperson info
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @NotNull(message = "Role is required")
    private SalesRole role;

    @Size(max = 100, message = "Zone cannot exceed 100 characters")
    private String zone;

    @Size(max = 100, message = "Region cannot exceed 100 characters")
    private String region;

    private Long managerId;

    // User account fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Status is required")
    private UserStatus status;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    // Contact information
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String contactNo;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Alternate contact must be a valid 10-digit Indian mobile number")
    private String alternateContactNo;

    @Size(max = 10, message = "Blood group cannot exceed 10 characters")
    private String bloodGroup;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String completeAddress;

    private LocalDate dateOfBirth;

    @Size(max = 10, message = "Gender cannot exceed 10 characters")
    private String gender;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 20, message = "ZIP code cannot exceed 20 characters")
    private String zip;

    // Email (for both salesperson and user)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Size(max = 50, message = "Employee roll number cannot exceed 50 characters")
    private String employeeRollNo;
}
