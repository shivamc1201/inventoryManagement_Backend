package com.nector.userservice.dto;

import com.nector.userservice.enums.RoleCategory;
import com.nector.userservice.model.Role;
import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String roleType;
    private String name;
    private String description;
    private RoleCategory roleCategory;

    public static RoleResponse from(Role role) {
        RoleResponse dto = new RoleResponse();
        dto.id = role.getId();
        dto.roleType = role.getRoleType();
        dto.name = role.getName();
        dto.description = role.getDescription();
        dto.roleCategory = role.getRoleCategory();
        return dto;
    }
}
