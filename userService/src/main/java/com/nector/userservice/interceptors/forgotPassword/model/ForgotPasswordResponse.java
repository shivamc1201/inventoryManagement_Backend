package com.nector.userservice.interceptors.forgotPassword.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Forgot password response")
public class ForgotPasswordResponse {
    @Schema(description = "Response message", example = "Password updated successfully")
    private String message;
    
    @Schema(description = "Username", example = "johndoe123")
    private String username;
    
    @Schema(description = "Status", example = "SUCCESS")
    private String status;
    
    public ForgotPasswordResponse(String message, String username, String status) {
        this.message = message;
        this.username = username;
        this.status = status;
    }
}