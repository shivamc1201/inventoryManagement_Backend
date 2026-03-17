package com.nector.userservice.dto;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private String firstName;
    private String lastName;
    private LocalDateTime createdOn;
    private String contactNo;
    private String alternateContactNo;
    private String bloodGroup;
    private String completeAddress;
    private LocalDate dateOfBirth;
    private String gender;
    private String city;
    private String country;
    private String zip;
    private LocalDateTime lastLoginTime;
    private boolean isLoggedIn;
    private LocalDateTime passwordSetDate;
    private RoleType roleType;
}
