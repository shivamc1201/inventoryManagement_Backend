package com.nector.userservice.interceptors.superAdmin.model;

import lombok.Data;

@Data
public class SuperAdminLoginResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
}