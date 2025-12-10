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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleManagementService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserApprovalRepository userApprovalRepository;
    
    @Transactional
    public void assignRoleToUser(Long userId, RoleType roleType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
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
    }
    
    @Transactional
    public void removeRoleFromUser(Long userId, RoleType roleType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        user.getRoles().remove(role);
        userRepository.save(user);
    }
    
    @Transactional
    public void assignPermissionToRole(RoleType roleType, Features feature) {
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Permission permission = permissionRepository.findByFeature(feature)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }
    
    @Transactional
    public void removePermissionFromRole(RoleType roleType, Features feature) {
        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Permission permission = permissionRepository.findByFeature(feature)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        role.getPermissions().remove(permission);
        roleRepository.save(role);
    }
    
    @Transactional(readOnly = true)
    public Set<Role> getAllRoles() {
        return Set.copyOf(roleRepository.findAll());
    }
    
    @Transactional(readOnly = true)
    public Set<Permission> getAllPermissions() {
        return Set.copyOf(permissionRepository.findAll());
    }
}