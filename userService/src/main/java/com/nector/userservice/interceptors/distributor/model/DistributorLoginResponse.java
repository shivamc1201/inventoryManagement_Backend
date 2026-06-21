package com.nector.userservice.interceptors.distributor.model;

import com.nector.userservice.common.BaseLoginResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Distributor login response")
public class DistributorLoginResponse  implements BaseLoginResponse {

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

    @Schema(description = "LOGIN/LOGOUT status")
    private String loginStatus;

    @Schema(description = "FIRM_NAME")
    private String firmName;

    @Schema(description = "DIS_CODE")
    private String disCode;

    public DistributorLoginResponse(
            String token,
            String type,
            String username,
            String message,
            Long userId,
            String roleType,
            List<Object> features,
            Set<String> featureNames,
            String firmName,
            String disCode,
            String loginStatus) {

        this.token = token;
        this.type = type;
        this.username = username;
        this.message = message;
        this.userId = userId;
        this.loginStatus = loginStatus;
        this.roleType = roleType;
        this.features = features;
        this.featureNames = featureNames;
        this.firmName = firmName;
        this.disCode = disCode;
    }
}