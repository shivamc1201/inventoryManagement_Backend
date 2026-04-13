package com.nector.userservice.interceptors.userLogin.model;

import com.nector.userservice.common.BaseLoginResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Unified login response with feature permissions")
public class UnifiedLoginResponse implements BaseLoginResponse {
    
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Token type", example = "Bearer")
    private String type;
    
    @Schema(description = "Username", example = "admin")
    private String username;
    
    @Schema(description = "Login status message", example = "Login successful for User admin")
    private String message;
    
    @Schema(description = "User role type", example = "ADMIN")
    private String roleType;
    
    @Schema(description = "User ID", example = "3")
    private Long userId;
    
    @Schema(description = "User features with details")
    private List<Object> features;
    
    @Schema(description = "Feature names for quick lookup")
    private Set<String> featureNames;
    
    @Schema(description = "LOGIN/LOGOUT status", example = "LOGGED_IN")
    private String loginStatus;
    
    @Schema(description = "Role feature permissions with CRUD access")
    private List<FeaturePermissionDTO> roleFeaturePermissions;
    
    public UnifiedLoginResponse() {}
    
    public UnifiedLoginResponse(String token, String type, String username, String message,
                               String roleType, Long userId, List<Object> features,
                               Set<String> featureNames, String loginStatus,
                               List<FeaturePermissionDTO> roleFeaturePermissions) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.message = message;
        this.roleType = roleType;
        this.userId = userId;
        this.features = features;
        this.featureNames = featureNames;
        this.loginStatus = loginStatus;
        this.roleFeaturePermissions = roleFeaturePermissions;
    }
}
