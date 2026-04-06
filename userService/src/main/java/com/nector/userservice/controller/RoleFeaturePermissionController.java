package com.nector.userservice.controller;

import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    /**
     * Get all role-feature permissions
     */
    @GetMapping
    public ResponseEntity<List<RoleFeaturePermission>> getAllPermissions() {
        List<RoleFeaturePermission> permissions = permissionRepository.findAll();
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
    public ResponseEntity<RoleFeaturePermission> createOrUpdatePermission(
            @PathVariable Integer roleId,
            @PathVariable Integer featureId,
            @RequestBody Map<String, Boolean> permissions) {
        
        try {
            // Convert featureId to Features enum
            Features feature = findFeatureById(featureId);
            
            Optional<RoleFeaturePermission> existingOpt = 
                permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
            
            RoleFeaturePermission permission;
            if (existingOpt.isPresent()) {
                permission = existingOpt.get();
                permission.setCanCreate(permissions.getOrDefault("canCreate", false));
                permission.setCanRead(permissions.getOrDefault("canRead", false));
                permission.setCanUpdate(permissions.getOrDefault("canUpdate", false));
                permission.setCanDelete(permissions.getOrDefault("canDelete", false));
            } else {
                permission = new RoleFeaturePermission(roleId, featureId, feature);
                permission.setCanCreate(permissions.getOrDefault("canCreate", false));
                permission.setCanRead(permissions.getOrDefault("canRead", false));
                permission.setCanUpdate(permissions.getOrDefault("canUpdate", false));
                permission.setCanDelete(permissions.getOrDefault("canDelete", false));
            }
            
            RoleFeaturePermission saved = permissionRepository.save(permission);
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("Error creating/updating permission for roleId: {}, featureId: {}", roleId, featureId, e);
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
    public ResponseEntity<List<RoleFeaturePermission>> bulkUpdatePermissions(
            @PathVariable Integer roleId,
            @RequestBody Map<Integer, Map<String, Boolean>> featurePermissions) {
        
        try {
            List<RoleFeaturePermission> updatedPermissions = featurePermissions.entrySet().stream()
                .map(entry -> {
                    Integer featureId = entry.getKey();
                    Map<String, Boolean> perms = entry.getValue();
                    
                    Features feature = findFeatureById(featureId);
                    
                    Optional<RoleFeaturePermission> existingOpt = 
                        permissionRepository.findByRoleIdAndFeatureId(roleId, featureId);
                    
                    RoleFeaturePermission permission;
                    if (existingOpt.isPresent()) {
                        permission = existingOpt.get();
                    } else {
                        permission = new RoleFeaturePermission(roleId, featureId, feature);
                    }
                    
                    permission.setCanCreate(perms.getOrDefault("canCreate", false));
                    permission.setCanRead(perms.getOrDefault("canRead", false));
                    permission.setCanUpdate(perms.getOrDefault("canUpdate", false));
                    permission.setCanDelete(perms.getOrDefault("canDelete", false));
                    
                    return permissionRepository.save(permission);
                })
                .collect(Collectors.toList());
            
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
            default -> throw new IllegalArgumentException("Unknown feature ID: " + featureId);
        };
    }
}
