package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.RoleFeatureMapping;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.User;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Security service for Spring Security RBAC integration
 * Provides CRUD permission checking based on user roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final UserRepository userRepository;
    private final RoleFeaturePermissionRepository roleFeaturePermissionRepository;
    
    @Transactional(readOnly = true)
    public boolean currentUserCanCreate(String resourceType) {
        log.debug("Checking create permission for resourceType: {}", resourceType);
        return hasPermission(getCurrentUserId(), "CREATE", resourceType);
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanRead(String resourceType) {
        log.debug("Checking read permission for resourceType: {}", resourceType);
        return hasPermission(getCurrentUserId(), "READ", resourceType);
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanUpdate(String resourceType) {
        log.debug("Checking update permission for resourceType: {}", resourceType);
        return hasPermission(getCurrentUserId(), "UPDATE", resourceType);
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanDelete(String resourceType) {
        log.debug("Checking delete permission for resourceType: {}", resourceType);
        return hasPermission(getCurrentUserId(), "DELETE", resourceType);
    }
    
    /**
     * Get current user ID from security context
     * TODO: Replace with actual Spring Security context implementation
     */
    private Long getCurrentUserId() {
        // For now, return test user ID - replace with actual implementation
        return 1L;
    }
    
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String operation, String resourceType) {
        log.debug("Checking permission - userId: {}, operation: {}, resourceType: {}", userId, operation, resourceType);
        
        try {
            User user = userRepository.findById(userId)
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found: {}", userId);
                return false;
            }
            
            // Convert resourceType to Features enum
            Features feature;
            try {
                feature = Features.valueOf(resourceType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown feature: {}", resourceType);
                return false;
            }
            
            // Get role ID
            Integer roleId = RoleFeatureMapping.getRoleId(user.getRoleType());
            Integer featureId = RoleFeatureMapping.getFeatureId(feature);
            
            // Check role-feature permission
            Optional<RoleFeaturePermission> permissionOpt = 
                roleFeaturePermissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
            
            if (permissionOpt.isPresent()) {
                boolean canPerform = permissionOpt.get().hasPermission(operation);
                log.debug("Permission check result: {} for userId: {}, operation: {}, resourceType: {}", 
                    canPerform, userId, operation, resourceType);
                return canPerform;
            }
            
            // Fallback to role-based default permissions if no specific mapping found
            boolean canPerform = switch (user.getRoleType()) {
                case SUPER_ADMIN, ADMIN -> {
                    // Admin can do everything
                    yield true;
                }
                case BUSINESS_DEV_MGR, PLANT_MGR, HR_MGR, LOGISTICS_MGR, 
                     ACCOUNT_MGR, NATIONAL_SALES_MGR, STATE_SALES_MGR, 
                     ZONAL_SALES_MGR, REGIONAL_SALES_MGR, AREA_SALES_MGR -> {
                    // Managers can create, read, update but not delete
                    yield !"DELETE".equals(operation);
                }
                case ACCOUNT_OFFICER, ACCOUNT_EXECUTIVE, SALES_OFFICER, 
                     SALES_EXECUTIVE, LOGISTICS_OFFICER, DISPATCH, HR_EXECUTIVE, 
                     PLANT_OFFICER, PLANT_EXECUTIVE -> {
                    // Officers can only read and update
                    yield !"CREATE".equals(operation) && !"DELETE".equals(operation);
                }
                case Distributor -> {
                    // Distributors can only read
                    yield "READ".equals(operation);
                }
                default -> false;
            };
            
            log.debug("Fallback permission check result: {} for userId: {}, operation: {}, resourceType: {}", 
                canPerform, userId, operation, resourceType);
            return canPerform;
            
        } catch (Exception e) {
            log.error("Error checking permission for userId: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Get all features accessible to current user for a specific operation
     */
    @Transactional(readOnly = true)
    public java.util.List<Features> getCurrentUserFeatures(String operation) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        
        Integer roleId = RoleFeatureMapping.getRoleId(user.getRoleType());
        
        return switch (operation.toUpperCase()) {
            case "CREATE" -> roleFeaturePermissionRepository.findCreatableFeaturesByRoleId(roleId)
                .stream().map(RoleFeaturePermission::getFeature).toList();
            case "READ" -> roleFeaturePermissionRepository.findReadableFeaturesByRoleId(roleId)
                .stream().map(RoleFeaturePermission::getFeature).toList();
            case "UPDATE" -> roleFeaturePermissionRepository.findUpdatableFeaturesByRoleId(roleId)
                .stream().map(RoleFeaturePermission::getFeature).toList();
            case "DELETE" -> roleFeaturePermissionRepository.findDeletableFeaturesByRoleId(roleId)
                .stream().map(RoleFeaturePermission::getFeature).toList();
            default -> java.util.Collections.emptyList();
        };
    }
}
