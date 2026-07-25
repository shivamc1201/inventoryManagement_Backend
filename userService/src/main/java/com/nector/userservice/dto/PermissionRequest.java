package com.nector.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request DTO for role-feature permissions")
public class PermissionRequest {
    
    @Schema(description = "Permission to read/view records", example = "true")
    private Boolean canRead = false;

    @Schema(description = "Permission to update existing records", example = "false")
    private Boolean canUpdate = false;
}
