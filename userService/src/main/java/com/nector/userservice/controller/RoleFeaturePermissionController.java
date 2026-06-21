package com.nector.userservice.controller;

import com.nector.userservice.common.features.Features;
import com.nector.userservice.common.RoleFeatureMapping;
import com.nector.userservice.dto.PermissionRequest;
import com.nector.userservice.interceptors.userLogin.model.FeaturePermissionDTO;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import com.nector.userservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing role-feature permissions
 * Provides CRUD operations for permission management
 */
@RestController
@RequestMapping("/api/role-feature-permissions")
@RequiredArgsConstructor
@Slf4j
public class RoleFeaturePermissionController {
    
    private final RoleFeaturePermissionRepository permissionRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all role-feature permissions
     */
    @GetMapping
    public ResponseEntity<List<RoleFeaturePermission>> getAllPermissions() {
        List<RoleFeaturePermission> permissions = permissionRepository.findAll();
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get all available features for frontend selection
     */
    @GetMapping("/features")
    @Operation(summary = "Get all available features", description = "Retrieves all available features with their IDs and display names for frontend selection")
    @ApiResponse(responseCode = "200", description = "Features retrieved successfully")
    public ResponseEntity<List<Map<String, Object>>> getAllFeatures() {
        List<Map<String, Object>> features = new ArrayList<>();
        
        for (Features feature : Features.values()) {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.put("id", com.nector.userservice.common.RoleFeatureMapping.getFeatureId(feature));
            featureMap.put("name", feature.name());
            featureMap.put("displayName", feature.getDisplayName());
            featureMap.put("path", feature.getPath());
            features.add(featureMap);
        }
        
        return ResponseEntity.ok(features);
    }
    
    /**
     * Get permissions by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeaturePermissionDTO>> getPermissionsByUser(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        List<FeaturePermissionDTO> permissions = getUserPermissions(user);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permissions by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<List<FeaturePermissionDTO>> getPermissionsByUsername(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        List<FeaturePermissionDTO> permissions = getUserPermissions(user);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permissions by role ID
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RoleFeaturePermission>> getPermissionsByRole(@PathVariable Integer roleId) {
        List<RoleFeaturePermission> permissions = permissionRepository.findByRoleId(roleId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get permissions by feature
     */
    @GetMapping("/feature/{feature}")
    public ResponseEntity<List<RoleFeaturePermission>> getPermissionsByFeature(@PathVariable Features feature) {
        List<RoleFeaturePermission> permissions = permissionRepository.findByFeatureId(
            com.nector.userservice.common.RoleFeatureMapping.getFeatureId(feature));
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get specific role-feature permission
     */
    @GetMapping("/role/{roleId}/feature/{featureId}")
    public ResponseEntity<RoleFeaturePermission> getRoleFeaturePermission(
            @PathVariable Integer roleId, 
            @PathVariable Integer featureId) {
        
        Optional<RoleFeaturePermission> permission = 
            permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
        
        return permission.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create or update role-feature permission
     */
    @PutMapping("/role/{roleId}/feature/{featureId}")
    @Operation(summary = "Create or update permission", description = "Creates or updates role-feature permission with specific CRUD permissions")
    @ApiResponse(responseCode = "200", description = "Permission updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<RoleFeaturePermission> createOrUpdatePermission(
            @PathVariable Integer roleId,
            @PathVariable Integer featureId,
            @RequestBody PermissionRequest permissionRequest) {
        
        try {
            // Convert featureId to Features enum
            Features feature = findFeatureById(featureId);
            
            Optional<RoleFeaturePermission> existingOpt = 
                permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
            
            RoleFeaturePermission permission;
            if (existingOpt.isPresent()) {
                permission = existingOpt.get();
            } else {
                permission = new RoleFeaturePermission(roleId, featureId, feature);
            }
            
            // Set permissions from the request DTO
            permission.setCanCreate(permissionRequest.getCanCreate() != null ? permissionRequest.getCanCreate() : false);
            permission.setCanRead(permissionRequest.getCanRead() != null ? permissionRequest.getCanRead() : false);
            permission.setCanUpdate(permissionRequest.getCanUpdate() != null ? permissionRequest.getCanUpdate() : false);
            permission.setCanDelete(permissionRequest.getCanDelete() != null ? permissionRequest.getCanDelete() : false);
            
            RoleFeaturePermission saved = permissionRepository.save(permission);
            log.info("Permission updated successfully for roleId: {}, featureId: {}, create: {}, read: {}, update: {}, delete: {}", 
                    roleId, featureId, permission.getCanCreate(), permission.getCanRead(), 
                    permission.getCanUpdate(), permission.getCanDelete());
            
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("Error creating/updating permission for roleId: {}, featureId: {}", roleId, featureId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create or update user-feature permission
     */
    @PutMapping("/user/{userId}/feature/{featureId}")
    @Operation(summary = "Create or update user permission", description = "Creates or updates user-feature permission based on user's role")
    @ApiResponse(responseCode = "200", description = "User permission updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<RoleFeaturePermission> createOrUpdateUserPermission(
            @PathVariable Long userId,
            @PathVariable Integer featureId,
            @RequestBody PermissionRequest permissionRequest) {
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            Integer roleId = getUserRoleId(user);
            
            if (roleId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Delegate to the existing role-based method
            return createOrUpdatePermission(roleId, featureId, permissionRequest);
            
        } catch (Exception e) {
            log.error("Error creating/updating user permission for userId: {}, featureId: {}", userId, featureId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete role-feature permission
     */
    @DeleteMapping("/role/{roleId}/feature/{featureId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer roleId, @PathVariable Integer featureId) {
        Optional<RoleFeaturePermission> permission = 
            permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
        
        if (permission.isPresent()) {
            permissionRepository.delete(permission.get());
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Get permission matrix for all roles and features
     */
    @GetMapping("/matrix")
    public ResponseEntity<Map<Integer, Map<Integer, RoleFeaturePermission>>> getPermissionMatrix() {
        List<RoleFeaturePermission> allPermissions = permissionRepository.findAll();
        
        Map<Integer, Map<Integer, RoleFeaturePermission>> matrix = allPermissions.stream()
            .collect(Collectors.groupingBy(
                RoleFeaturePermission::getRoleId,
                Collectors.toMap(RoleFeaturePermission::getFeatureId, p -> p)
            ));
        
        return ResponseEntity.ok(matrix);
    }
    
    /**
     * Bulk update permissions for a role
     */
    @PutMapping("/role/{roleId}/bulk")
    @Operation(summary = "Bulk update permissions", description = "Updates multiple permissions for a role in a single request")
    @ApiResponse(responseCode = "200", description = "Permissions updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<List<RoleFeaturePermission>> bulkUpdatePermissions(
            @PathVariable Integer roleId,
            @RequestBody Map<Integer, PermissionRequest> featurePermissions) {
        
        try {
            List<RoleFeaturePermission> updatedPermissions = featurePermissions.entrySet().stream()
                .map(entry -> {
                    Integer featureId = entry.getKey();
                    PermissionRequest perms = entry.getValue();
                    
                    Features feature = findFeatureById(featureId);
                    
                    Optional<RoleFeaturePermission> existingOpt = 
                        permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
                    
                    RoleFeaturePermission permission;
                    if (existingOpt.isPresent()) {
                        permission = existingOpt.get();
                    } else {
                        permission = new RoleFeaturePermission(roleId, featureId, feature);
                    }
                    
                    // Set permissions from the request DTO
                    permission.setCanCreate(perms.getCanCreate() != null ? perms.getCanCreate() : false);
                    permission.setCanRead(perms.getCanRead() != null ? perms.getCanRead() : false);
                    permission.setCanUpdate(perms.getCanUpdate() != null ? perms.getCanUpdate() : false);
                    permission.setCanDelete(perms.getCanDelete() != null ? perms.getCanDelete() : false);
                    
                    return permissionRepository.save(permission);
                })
                .collect(Collectors.toList());
            
            log.info("Bulk permissions updated successfully for roleId: {}, total features: {}", roleId, updatedPermissions.size());
            return ResponseEntity.ok(updatedPermissions);
            
        } catch (Exception e) {
            log.error("Error bulk updating permissions for roleId: {}", roleId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    private Features findFeatureById(Integer featureId) {
        // This is a simplified mapping - in production, you might want a more robust solution
        return switch (featureId) {
            case 1 -> Features.DASHBOARD;
            case 2 -> Features.ACCOUNTS;
            case 3 -> Features.HR;
            case 4 -> Features.DISTRIBUTOR;
            case 5 -> Features.INVENTORY;
            case 6 -> Features.SALES;
            case 7 -> Features.REPORTS;
            case 8 -> Features.COMPLAINT;
            case 9 -> Features.PRODUCTS;
            case 10 -> Features.ORDER_DETAILS;
            case 11 -> Features.LOGISTIC;
            case 12 -> Features.USER_RIGHTS;
            case 13 -> Features.DISPATCH;
            case 14 -> Features.PRODUCTS_FINISHED_PRODUCTS;
            case 15 -> Features.PRODUCTS_RAW_MATERIALS;
            case 16 -> Features.PRODUCTS_MACHINE_PARTS;
            case 17 -> Features.INVENTORY_MASTERS;
            case 18 -> Features.INVENTORY_TRANSACTIONS;
            case 19 -> Features.INVENTORY_TRANSACTIONS_PROFORMA_INVOICE;
            case 20 -> Features.INVENTORY_TRANSACTIONS_PO_DEPOSIT_RECEIPTS_LIST;
            case 21 -> Features.INVENTORY_TRANSACTIONS_PO_LIST;
            case 22 -> Features.INVENTORY_TRANSACTIONS_OUTWARD_CHALLAN;
            case 23 -> Features.INVENTORY_TRANSACTIONS_SALE_INVOICE;
            case 24 -> Features.INVENTORY_INWARD;
            case 25 -> Features.INVENTORY_OUTWARD;
            case 26 -> Features.TRANSACTION_CASHBOOK;
            case 27 -> Features.TRANSACTION_MASTER;
            default -> throw new IllegalArgumentException("Unknown feature ID: " + featureId);
        };
    }
    
    /**
     * Get user permissions based on their role(s)
     */
    private List<FeaturePermissionDTO> getUserPermissions(User user) {
        List<FeaturePermissionDTO> allPermissions = new ArrayList<>();
        
        // Get permissions from primary roleType
        if (user.getRoleType() != null) {
            Integer roleId = RoleFeatureMapping.getRoleId(user.getRoleType());
            List<FeaturePermissionDTO> roleTypePermissions = getPermissionsByRoleId(roleId);
            allPermissions.addAll(roleTypePermissions);
        }
        
        // Get permissions from additional roles (many-to-many)
        if (!user.getRoles().isEmpty()) {
            for (var role : user.getRoles()) {
                Integer roleId = RoleFeatureMapping.getRoleId(role.getRoleType());
                List<FeaturePermissionDTO> rolePermissions = getPermissionsByRoleId(roleId);
                allPermissions.addAll(rolePermissions);
            }
        }
        
        // Remove duplicates based on roleId and featureId
        return allPermissions.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Get permissions by role ID and convert to DTO
     */
    private List<FeaturePermissionDTO> getPermissionsByRoleId(Integer roleId) {
        return permissionRepository.findByRoleId(roleId)
                .stream()
                .map(perm -> new FeaturePermissionDTO(
                        perm.getRoleId(),
                        perm.getFeatureId(),
                        perm.getFeature().name(),
                        perm.getCanCreate(),
                        perm.getCanRead(),
                        perm.getCanUpdate(),
                        perm.getCanDelete()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get user's role ID (from primary roleType or first additional role)
     */
    private Integer getUserRoleId(User user) {
        if (user.getRoleType() != null) {
            return RoleFeatureMapping.getRoleId(user.getRoleType());
        } else if (!user.getRoles().isEmpty()) {
            return RoleFeatureMapping.getRoleId(user.getRoles().iterator().next().getRoleType());
        }
        return null;
    }
}
