package com.nector.userservice.interceptors.userLogin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "User login response")
public class LoginResponse {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Token type", example = "Bearer")
    private String type;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Login status message", example = "Login successful")
    private String message;
    
    @Schema(description = "User role type")
    private String roleType;
    
    @Schema(description = "User ID")
    private Long userId;
    
    @Schema(description = "User features with details")
    private List<Object> features;
    
    @Schema(description = "Feature names for quick lookup")
    private Set<String> featureNames;
    
    public LoginResponse(String token, String type, String username, String message) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.message = message;
    }
    
    public LoginResponse(String token, String type, String username, String message, String roleType, Long userId, List<Object> features, Set<String> featureNames) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.message = message;
        this.roleType = roleType;
        this.userId = userId;
        this.features = features;
        this.featureNames = featureNames;
    }
}