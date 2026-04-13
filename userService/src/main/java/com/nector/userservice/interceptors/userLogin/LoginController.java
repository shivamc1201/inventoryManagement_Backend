package com.nector.userservice.interceptors.userLogin;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.dto.UserDetailsDTO;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.model.UnifiedLoginResponse;
import com.nector.userservice.interceptors.userLogin.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username and password and return permissions")
    @ApiResponse(responseCode = "200", description = "Login successful with permissions")
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UnifiedLoginResponse response = loginService.authenticateWithPermissions(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/secondUser")
    @ApiResponse(responseCode = "200", description = "Second user login successful")
    public ResponseEntity<?> loginSecondUser(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = loginService.authenticateSecondUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userprofile}")
    @Operation(summary = "Get user details by username", description = "Retrieve user details using username")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<?> getUserDetailsByUsername(@PathVariable String username) {
        try {
            UserDetailsDTO userDetails = loginService.getUserDetailsByUsername(username);
            return ResponseEntity.ok(userDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}