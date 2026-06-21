package com.nector.userservice.interceptors.forgotPassword.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Forgot password request")
public class ForgotPasswordRequest {
    @NotBlank(message = "New password is required")
    @Schema(description = "New password", example = "NewPass123!")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "NewPass123!")
    private String confirmPassword;
}