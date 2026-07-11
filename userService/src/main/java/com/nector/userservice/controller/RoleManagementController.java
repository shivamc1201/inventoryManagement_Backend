package com.nector.userservice.controller;

import com.nector.userservice.model.Role;
import com.nector.userservice.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing user roles (posts)")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    @PostMapping("/create")
    @Operation(summary = "Create a new role (post)", description = "Creates a new role type that can be assigned to users")
    @ApiResponse(responseCode = "200", description = "Role created successfully")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> body) {
        try {
            String roleType = body.get("roleType");
            String name = body.get("name");
            String description = body.get("description");
            if (roleType == null || roleType.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "roleType is required"));
            }
            Role created = roleManagementService.createRole(roleType.toUpperCase().replace(" ", "_"), name, description);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-user")
    @Operation(summary = "Assign role to user", description = "Assigns a specific role to a user")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully")
    public ResponseEntity<String> assignRoleToUser(
            @RequestParam Long userId,
            @RequestParam String roleType) {
        roleManagementService.assignRoleToUser(userId, roleType);
        return ResponseEntity.ok("Role assigned successfully: " + roleType);
    }

    @DeleteMapping("/remove-user")
    @Operation(summary = "Remove role from user", description = "Removes a specific role from a user")
    @ApiResponse(responseCode = "200", description = "Role removed successfully")
    public ResponseEntity<String> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam String roleType) {
        roleManagementService.removeRoleFromUser(userId, roleType);
        return ResponseEntity.ok("Role removed successfully");
    }

    @GetMapping("/all")
    @Operation(summary = "Get all roles", description = "Retrieves all available roles (posts) in the system")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }
}
