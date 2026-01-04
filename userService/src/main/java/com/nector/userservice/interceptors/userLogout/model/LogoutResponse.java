package com.nector.userservice.interceptors.userLogout.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutResponse {
    private String message;

    @Schema(description = "LOGIN/LOGOUT status")
    private String loginStatus;
}