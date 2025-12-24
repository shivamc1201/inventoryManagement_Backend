package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import com.nector.userservice.model.Role;
import com.nector.userservice.repository.PermissionRepository;
import com.nector.userservice.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnumSyncService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @PostConstruct
    @Transactional
    public void syncEnumsWithDatabase() {
        log.info("Entering syncEnumsWithDatabase()");
        
        syncRoles();
        syncPermissions();
        log.info("Enum synchronization completed");
        
        log.info("Exiting syncEnumsWithDatabase()");
    }
    
    private void syncRoles() {
        log.debug("Entering syncRoles()");
        
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findByRoleType(roleType)
                .orElseGet(() -> {
                    Role role = new Role(roleType, roleType.name(), 
                        "Role for " + roleType.getDepartment() + " department");
                    Role saved = roleRepository.save(role);
                    log.info("Created new role: {}", roleType);
                    return saved;
                });
        }
        
        log.debug("Exiting syncRoles()");
    }
    
    private void syncPermissions() {
        log.debug("Entering syncPermissions()");
        
        for (Features feature : Features.values()) {
            permissionRepository.findByFeature(feature)
                .orElseGet(() -> {
                    Permission permission = new Permission(feature, feature.getDisplayName(), 
                        "Permission for " + feature.getDisplayName(), feature.getPath());
                    Permission saved = permissionRepository.save(permission);
                    log.info("Created new permission: {}", feature);
                    return saved;
                });
        }
        
        log.debug("Exiting syncPermissions()");
    }
}