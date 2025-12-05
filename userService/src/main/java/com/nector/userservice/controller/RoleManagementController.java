package com.nector.userservice.controller;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import com.nector.userservice.model.Role;
import com.nector.userservice.service.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
// @PreAuthorize("hasAuthority('FEATURE_USER_RIGHTS')")
public class RoleManagementController {
    
    private final RoleManagementService roleManagementService;
    
    @PostMapping("/assign-user")
    public ResponseEntity<String> assignRoleToUser(
            @RequestParam Long userId, 
            @RequestParam RoleType roleType) {
        roleManagementService.assignRoleToUser(userId, roleType);
        return ResponseEntity.ok("Role assigned successfully");
    }
    
    @DeleteMapping("/remove-user")
    public ResponseEntity<String> removeRoleFromUser(
            @RequestParam Long userId, 
            @RequestParam RoleType roleType) {
        roleManagementService.removeRoleFromUser(userId, roleType);
        return ResponseEntity.ok("Role removed successfully");
    }
    
    @PostMapping("/assign-permission")
    public ResponseEntity<String> assignPermissionToRole(
            @RequestParam RoleType roleType, 
            @RequestParam Features feature) {
        roleManagementService.assignPermissionToRole(roleType, feature);
        return ResponseEntity.ok("Permission assigned successfully");
    }
    
    @DeleteMapping("/remove-permission")
    public ResponseEntity<String> removePermissionFromRole(
            @RequestParam RoleType roleType, 
            @RequestParam Features feature) {
        roleManagementService.removePermissionFromRole(roleType, feature);
        return ResponseEntity.ok("Permission removed successfully");
    }
    
    @GetMapping("/all")
    public ResponseEntity<Set<Role>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }
    
    @GetMapping("/permissions/all")
    public ResponseEntity<Set<Permission>> getAllPermissions() {
        return ResponseEntity.ok(roleManagementService.getAllPermissions());
    }
}