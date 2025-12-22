package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import com.nector.userservice.model.Role;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
import com.nector.userservice.repository.PermissionRepository;
import com.nector.userservice.repository.RoleRepository;
import com.nector.userservice.repository.UserApprovalRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManagementService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserApprovalRepository userApprovalRepository;
    
    @Transactional
    public void assignRoleToUser(Long userId, RoleType roleType) {
        log.info("Entering assignRoleToUser() for userId: {}, roleType: {}", userId, roleType);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("Exiting assignRoleToUser() - User not found: {}", userId);
                return new RuntimeException("User not found");
            });
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> {
                log.warn("Exiting assignRoleToUser() - Role not found: {}", roleType);
                return new RuntimeException("Role not found");
            });
        
        user.getRoles().add(role);
        user.setStatus(UserStatus.ACTIVE);
        
        UserApproval approval = userApprovalRepository.findByUser(user)
            .orElseGet(() -> {
                UserApproval newApproval = new UserApproval();
                newApproval.setUser(user);
                return newApproval;
            });
        approval.setApprovalStatus("APPROVED");
        
        userApprovalRepository.save(approval);
        userRepository.save(user);
        
        log.info("Exiting assignRoleToUser() - Role assigned successfully for userId: {}, roleType: {}", userId, roleType);
    }
    
    @Transactional
    public void removeRoleFromUser(Long userId, RoleType roleType) {
        log.info("Entering removeRoleFromUser() for userId: {}, roleType: {}", userId, roleType);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("Exiting removeRoleFromUser() - User not found: {}", userId);
                return new RuntimeException("User not found");
            });
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> {
                log.warn("Exiting removeRoleFromUser() - Role not found: {}", roleType);
                return new RuntimeException("Role not found");
            });
        
        user.getRoles().remove(role);
        userRepository.save(user);
        
        log.info("Exiting removeRoleFromUser() - Role removed successfully for userId: {}, roleType: {}", userId, roleType);
    }
    
    @Transactional
    public void assignPermissionToRole(RoleType roleType, Features feature) {
        log.info("Entering assignPermissionToRole() for roleType: {}, feature: {}", roleType, feature);
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> {
                log.warn("Exiting assignPermissionToRole() - Role not found: {}", roleType);
                return new RuntimeException("Role not found");
            });
        
        Permission permission = permissionRepository.findByFeature(feature)
            .orElseThrow(() -> {
                log.warn("Exiting assignPermissionToRole() - Permission not found: {}", feature);
                return new RuntimeException("Permission not found");
            });
        
        role.getPermissions().add(permission);
        roleRepository.save(role);
        
        log.info("Exiting assignPermissionToRole() - Permission assigned successfully for roleType: {}, feature: {}", roleType, feature);
    }
    
    @Transactional
    public void removePermissionFromRole(RoleType roleType, Features feature) {
        log.info("Entering removePermissionFromRole() for roleType: {}, feature: {}", roleType, feature);
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> {
                log.warn("Exiting removePermissionFromRole() - Role not found: {}", roleType);
                return new RuntimeException("Role not found");
            });
        
        Permission permission = permissionRepository.findByFeature(feature)
            .orElseThrow(() -> {
                log.warn("Exiting removePermissionFromRole() - Permission not found: {}", feature);
                return new RuntimeException("Permission not found");
            });
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
        
        log.info("Exiting removePermissionFromRole() - Permission removed successfully for roleType: {}, feature: {}", roleType, feature);
    }
    
    @Transactional(readOnly = true)
    public Set<Role> getAllRoles() {
        log.info("Entering getAllRoles()");
        Set<Role> roles = Set.copyOf(roleRepository.findAll());
        log.info("Exiting getAllRoles() with {} roles", roles.size());
        return roles;
    }
    
    @Transactional(readOnly = true)
    public Set<Permission> getAllPermissions() {
        log.info("Entering getAllPermissions()");
        Set<Permission> permissions = Set.copyOf(permissionRepository.findAll());
        log.info("Exiting getAllPermissions() with {} permissions", permissions.size());
        return permissions;
    }
}