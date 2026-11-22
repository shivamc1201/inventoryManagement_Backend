package com.nector.userservice.interceptors.forgotPassword;

import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordRequest;
import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordResponse;
import com.nector.userservice.interceptors.forgotPassword.service.ForgotPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password/{username}")
    @Operation(summary = "Reset password", description = "Reset password with username in path")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @ApiResponse(responseCode = "400", description = "Username not found or password validation failed")
    public ResponseEntity<?> forgotPassword(@PathVariable String username, @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Entering forgotPassword() for username: {}", username);
        try {
            ForgotPasswordResponse response = forgotPasswordService.processForgotPassword(username, request);
            log.info("Exiting forgotPassword() - password reset successful for username: {}", username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Exiting forgotPassword() - failed for username: {} - {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}