package com.nector.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to create a new role")
public class CreateRoleRequest {

    @Schema(description = "Unique role type identifier", example = "LOGISTICS_HEAD", required = true)
    private String roleType;

    @Schema(description = "USER or SALES", example = "USER")
    private String roleCategory;
}
