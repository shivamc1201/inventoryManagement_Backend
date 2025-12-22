package com.nector.userservice.controller;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import com.nector.userservice.model.Role;
import com.nector.userservice.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing user roles and permissions")
public class RoleManagementController {
    
    private final RoleManagementService roleManagementService;
    
    @PostMapping("/assign-user")
    @Operation(summary = "Assign role to user", description = "Assigns a specific role to a user")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully")
    public ResponseEntity<String> assignRoleToUser(
            @RequestParam Long userId, 
            @RequestParam RoleType roleType) {
        roleManagementService.assignRoleToUser(userId, roleType);
        return ResponseEntity.ok("Role assigned successfully: " + roleType);
    }
    
    @DeleteMapping("/remove-user")
    @Operation(summary = "Remove role from user", description = "Removes a specific role from a user")
    @ApiResponse(responseCode = "200", description = "Role removed successfully")
    public ResponseEntity<String> removeRoleFromUser(
            @RequestParam Long userId, 
            @RequestParam RoleType roleType) {
        roleManagementService.removeRoleFromUser(userId, roleType);
        return ResponseEntity.ok("Role removed successfully");
    }
    
    @PostMapping("/assign-permission")
    @Operation(summary = "Assign permission to role", description = "Assigns a specific permission/feature to a role")
    @ApiResponse(responseCode = "200", description = "Permission assigned successfully")
    public ResponseEntity<String> assignPermissionToRole(
            @RequestParam RoleType roleType, 
            @RequestParam Features feature) {
        roleManagementService.assignPermissionToRole(roleType, feature);
        return ResponseEntity.ok("Permission assigned successfully");
    }
    
    @DeleteMapping("/remove-permission")
    @Operation(summary = "Remove permission from role", description = "Removes a specific permission/feature from a role")
    @ApiResponse(responseCode = "200", description = "Permission removed successfully")
    public ResponseEntity<String> removePermissionFromRole(
            @RequestParam RoleType roleType, 
            @RequestParam Features feature) {
        roleManagementService.removePermissionFromRole(roleType, feature);
        return ResponseEntity.ok("Permission removed successfully");
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all roles", description = "Retrieves all available roles in the system")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public ResponseEntity<Set<Role>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }
    
    @GetMapping("/permissions/all")
    @Operation(summary = "Get all permissions", description = "Retrieves all available permissions in the system")
    @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
    public ResponseEntity<Set<Permission>> getAllPermissions() {
        return ResponseEntity.ok(roleManagementService.getAllPermissions());
    }
}