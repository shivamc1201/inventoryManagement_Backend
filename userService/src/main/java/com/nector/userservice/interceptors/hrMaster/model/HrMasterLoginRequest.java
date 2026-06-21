package com.nector.userservice.interceptors.hrMaster.model;

import lombok.Data;

@Data
public class HrMasterLoginRequest {
    private String username;
    private String password;
}