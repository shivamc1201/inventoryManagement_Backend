package com.nector.userservice.interceptors.hrMaster.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PendingUserEditRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String alternateContactNo;
    private String bloodGroup;
    private String completeAddress;
    private String city;
    private String country;
    private String zip;
    private LocalDate dateOfBirth;
    private String gender;
    private String employeeRollNo;

    // SALES-only corrections
    private String zone;
    private String region;
}
