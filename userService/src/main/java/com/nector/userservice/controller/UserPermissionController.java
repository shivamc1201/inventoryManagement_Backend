package com.nector.userservice.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.UserUpdateRequest;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.service.RbacService;
import com.nector.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "User Permissions", description = "APIs for managing user permissions and features")
public class UserPermissionController {
    
    private final RbacService rbacService;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @GetMapping("/current-user")
    @Operation(summary = "Get current user permissions", description = "Retrieves permissions and status for the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "User permissions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access forbidden")
    public ResponseEntity<Map<String, Object>> getCurrentUserPermissions() {
        try {
            Map<String, Object> userPermissions = rbacService.getCurrentUserPermissionsWithStatus();
            return ResponseEntity.ok(userPermissions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user permissions by ID", description = "Retrieves permissions and status for a specific user by their ID")
    @ApiResponse(responseCode = "200", description = "User permissions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access forbidden")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@PathVariable Long userId) {
        try {
            Map<String, Object> userPermissions = rbacService.getUserPermissionsWithStatus(userId);
            return ResponseEntity.ok(userPermissions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/all-features")
    @Operation(summary = "Get all features", description = "Retrieves all available features/permissions in the system")
    @ApiResponse(responseCode = "200", description = "Features retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAllFeatures() {
        Map<String, Object> response = Map.of(
            "features", java.util.Arrays.stream(Features.values())
                .map(feature -> Map.of(
                    "name", feature.name(),
                    "displayName", feature.getDisplayName(),
                    "path", feature.getPath()
                ))
                .collect(Collectors.toList())
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all-users")
    @Operation(summary = "Get all users", description = "Retrieves all users in the system")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedOn(user.getCreatedOn());
        response.setRoleType(user.getRoleType());
        return response;
    }

    //TODO wee will add @preauth to only allow superAdmin
    @PatchMapping("/user_edit/{userId}")
    @Operation(summary = "Update user", description = "Updates user information and status")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {

        User updatedUser = userService.updateUser(userId, request);

        return ResponseEntity.ok(
                Map.of("message", "User updated successfully", "username", updatedUser.getUsername())
        );
    }

}