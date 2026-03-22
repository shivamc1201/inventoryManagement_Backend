package com.nector.userservice.dto;

import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.common.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SalesPersonResponse {

    private Long id;
    
    // Basic salesperson info
    private String name;
    private SalesRole role;
    private String zone;
    private String region;
    private Long managerId;
    
    // User account fields
    private String username;
    private UserStatus status;
    private String firstName;
    private String lastName;
    
    // Contact information
    private String contactNo;
    private String alternateContactNo;
    private String phone; // Legacy field
    private String email;
    
    // Personal details
    private String bloodGroup;
    private String completeAddress;
    private LocalDate dateOfBirth;
    private String gender;
    private String city;
    private String country;
    private String zip;
    
    // Work details
    private String employeeRollNo;
    
    // Timestamps
    private LocalDateTime createdAt;
    private Boolean active;
}
