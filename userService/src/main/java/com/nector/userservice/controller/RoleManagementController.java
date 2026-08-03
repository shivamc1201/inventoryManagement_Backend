package com.nector.userservice.controller;

import com.nector.userservice.dto.CreateRoleRequest;
import com.nector.userservice.dto.RoleResponse;
import com.nector.userservice.enums.RoleCategory;
import com.nector.userservice.model.Role;
import com.nector.userservice.repository.RoleRepository;
import com.nector.userservice.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing user roles (posts)")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;
    private final RoleRepository roleRepository;

    @PostMapping("/create")
    @Operation(summary = "Create a new role", description = "Creates a new role type that can be assigned to users")
    @ApiResponse(responseCode = "200", description = "Role created successfully")
    public ResponseEntity<?> createRole(@RequestBody CreateRoleRequest body) {
        log.info("Entering createRole() with roleType: {}, roleCategory: {}", body.getRoleType(), body.getRoleCategory());
        try {
            if (body.getRoleType() == null || body.getRoleType().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "roleType is required"));
            }
            RoleCategory roleCategory = null;
            if (body.getRoleCategory() != null && !body.getRoleCategory().isBlank()) {
                try {
                    roleCategory = RoleCategory.valueOf(body.getRoleCategory().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "roleCategory must be USER or SALES"));
                }
            }
            Role created = roleManagementService.createRole(
                    body.getRoleType().toUpperCase().replace(" ", "_"),
                    null,
                    null,
                    roleCategory);
            log.info("Exiting createRole() - created role: {}", created.getRoleType());
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            log.warn("Exiting createRole() - failed to create role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-user")
    @Operation(summary = "Assign role to user", description = "Assigns a specific role to a user")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully")
    public ResponseEntity<String> assignRoleToUser(
            @RequestParam Long userId,
            @RequestParam String roleType) {
        log.info("Entering assignRoleToUser() - userId: {}, roleType: {}", userId, roleType);
        roleManagementService.assignRoleToUser(userId, roleType);
        log.info("Exiting assignRoleToUser() - role {} assigned to userId: {}", roleType, userId);
        return ResponseEntity.ok("Role assigned successfully: " + roleType);
    }

    @DeleteMapping("/remove-user")
    @Operation(summary = "Remove role from user", description = "Removes a specific role from a user")
    @ApiResponse(responseCode = "200", description = "Role removed successfully")
    public ResponseEntity<String> removeRoleFromUser(
            @RequestParam Long userId,
            @RequestParam String roleType) {
        log.info("Entering removeRoleFromUser() - userId: {}, roleType: {}", userId, roleType);
        roleManagementService.removeRoleFromUser(userId, roleType);
        log.info("Exiting removeRoleFromUser() - role {} removed from userId: {}", roleType, userId);
        return ResponseEntity.ok("Role removed successfully");
    }

    @GetMapping("/all")
    @Operation(summary = "Get all roles", description = "Retrieves all available roles in the system")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.info("Entering getAllRoles()");
        List<RoleResponse> roles = roleManagementService.getAllRoles().stream().map(RoleResponse::from).toList();
        log.info("Exiting getAllRoles() - returned {} roles", roles.size());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get roles by category", description = "Returns USER roles or SALES roles for onboarding dropdown")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public ResponseEntity<?> getRolesByCategory(@RequestParam String category) {
        log.info("Entering getRolesByCategory() with category: {}", category);
        try {
            RoleCategory roleCategory = RoleCategory.valueOf(category.toUpperCase());
            List<RoleResponse> roles = roleRepository.findByRoleCategory(roleCategory)
                    .stream().map(RoleResponse::from).toList();
            log.info("Exiting getRolesByCategory() - returned {} roles for category: {}", roles.size(), category);
            return ResponseEntity.ok(roles);
        } catch (IllegalArgumentException e) {
            log.warn("Exiting getRolesByCategory() - invalid category: {}", category);
            return ResponseEntity.badRequest().body(Map.of("error", "category must be USER or SALES"));
        }
    }
}
