package com.nector.userservice.interceptors.superAdmin.model;

import lombok.Data;

@Data
public class SuperAdminLoginRequest {
    private String username;
    private String password;
}