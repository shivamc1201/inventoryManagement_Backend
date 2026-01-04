package com.nector.userservice.interceptors.userLogout;

import com.nector.userservice.interceptors.userLogout.model.LogoutRequest;
import com.nector.userservice.interceptors.userLogout.model.LogoutResponse;
import com.nector.userservice.interceptors.userLogout.service.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class LogoutController {
    
    private final LogoutService logoutService;
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate token")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "400", description = "Invalid token")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            LogoutResponse response = logoutService.logoutUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}