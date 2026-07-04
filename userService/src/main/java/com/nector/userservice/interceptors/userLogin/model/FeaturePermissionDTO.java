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

    @Schema(description = "Can create permission", example = "false")
    private Boolean canCreate;

    @Schema(description = "Can read permission", example = "true")
    private Boolean canRead;

    @Schema(description = "Can update permission", example = "false")
    private Boolean canUpdate;

    @Schema(description = "Can delete permission", example = "false")
    private Boolean canDelete;

    public FeaturePermissionDTO() {}

    public FeaturePermissionDTO(Long userId, Integer featureId, String feature,
                                Boolean canCreate, Boolean canRead, Boolean canUpdate, Boolean canDelete) {
        this.userId = userId;
        this.featureId = featureId;
        this.feature = feature;
        this.canCreate = canCreate;
        this.canRead = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
    }
}
