package com.nector.userservice.interceptors.userCreate.model;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Schema(description = "User registration response")
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private String firstName;
    private String lastName;
    private String contactNo;
    private String alternateContactNo;
    private String bloodGroup;
    private String completeAddress;
    private LocalDate dateOfBirth;
    private String gender;
    private String city;
    private String country;
    private String zip;
    private LocalDateTime createdOn;
    private LocalDateTime lastLoginTime;
    private boolean isLoggedIn;
    private LocalDateTime passwordSetDate;
    private RoleType roleType;
    // Optional: expose roles only if required
    private Set<String> roles;
}
