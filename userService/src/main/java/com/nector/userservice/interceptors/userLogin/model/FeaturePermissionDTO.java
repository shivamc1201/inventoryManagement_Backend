package com.nector.userservice.interceptors.userLogin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Feature permission details")
public class FeaturePermissionDTO {

    @Schema(description = "User ID", example = "67")
    private Long userId;

    @Schema(description = "Feature ID", example = "1")
    private Integer featureId;

    @Schema(description = "Feature name", example = "DASHBOARD")
    private String feature;

    @Schema(description = "Can read permission", example = "true")
    private Boolean canRead;

    @Schema(description = "Can update permission", example = "false")
    private Boolean canUpdate;

    public FeaturePermissionDTO() {}

    public FeaturePermissionDTO(Long userId, Integer featureId, String feature,
                                Boolean canRead, Boolean canUpdate) {
        this.userId = userId;
        this.featureId = featureId;
        this.feature = feature;
        this.canRead = canRead;
        this.canUpdate = canUpdate;
    }
}
