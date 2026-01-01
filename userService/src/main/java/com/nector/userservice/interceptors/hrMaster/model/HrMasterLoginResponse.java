package com.nector.userservice.interceptors.hrMaster.model;

import lombok.Data;

@Data
public class HrMasterLoginResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
}