package com.nector.userservice.util;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import com.nector.userservice.model.Role;
import com.nector.userservice.repository.PermissionRepository;
import com.nector.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnumMappingUtil {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Convert RoleType enum to Role entity
     */
    public Optional<Role> getRoleByEnum(RoleType roleType) {
        return roleRepository.findByRoleType(roleType);
    }
    
    /**
     * Convert Features enum to Permission entity
     */
    public Optional<Permission> getPermissionByEnum(Features feature) {
        return permissionRepository.findByFeature(feature);
    }
    
    /**
     * Get RoleType enum from Role entity
     */
    public RoleType getEnumFromRole(Role role) {
        return role.getRoleType();
    }
    
    /**
     * Get Features enum from Permission entity
     */
    public Features getEnumFromPermission(Permission permission) {
        return permission.getFeature();
    }
    
    /**
     * Check if enum exists in database
     */
    public boolean roleExists(RoleType roleType) {
        return roleRepository.findByRoleType(roleType).isPresent();
    }
    
    /**
     * Check if enum exists in database
     */
    public boolean permissionExists(Features feature) {
        return permissionRepository.findByFeature(feature).isPresent();
    }
}