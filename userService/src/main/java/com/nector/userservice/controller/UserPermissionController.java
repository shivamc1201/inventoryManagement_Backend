package com.nector.userservice.controller;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.UserUpdateRequest;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "User Permissions", description = "APIs for managing user permissions and features")
public class UserPermissionController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/all-features")
    @Operation(summary = "Get all features", description = "Retrieves all available features in the system")
    @ApiResponse(responseCode = "200", description = "Features retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAllFeatures() {
        Map<String, Object> response = Map.of(
            "features", Arrays.stream(Features.values())
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
            .filter(user -> !"SUPER_ADMIN".equals(user.getRoleType()))
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/user_edit/{userId}")
    @Operation(summary = "Update user", description = "Updates user information and status")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        User updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(
                Map.of("message", "User updated successfully", "username", updatedUser.getUsername(),
                        "status", updatedUser.getStatus(), "password", updatedUser.getPassword())
        );
    }

    @DeleteMapping("/user_suspend/{userId}")
    @Operation(summary = "Suspend user", description = "Suspends a specific user")
    @ApiResponse(responseCode = "200", description = "User suspended successfully")
    public ResponseEntity<?> removalOfUser(@PathVariable Long userId) {
        try {
            User updatedUser = userService.suspendUser(userId);
            return ResponseEntity.ok(
                    Map.of("message", "User suspended successfully", "username", updatedUser.getUsername())
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    @DeleteMapping("/user_delete/{userId}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user from the database")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Cannot delete user - user has related data"));
        }
    }

    private UserResponse mapToUserResponse(User savedUser) {
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setStatus(savedUser.getStatus());
        response.setContactNo(savedUser.getContactNo());
        response.setAlternateContactNo(savedUser.getAlternateContactNo());
        response.setBloodGroup(savedUser.getBloodGroup());
        response.setCompleteAddress(savedUser.getCompleteAddress());
        response.setGender(savedUser.getGender());
        response.setCity(savedUser.getCity());
        response.setCountry(savedUser.getCountry());
        response.setZip(savedUser.getZip());
        response.setRoleType(savedUser.getRoleType());
        response.setDateOfBirth(savedUser.getDateOfBirth());
        response.setCreatedOn(savedUser.getCreatedOn());
        response.setLastLoginTime(savedUser.getLastLoginTime());
        response.setLoggedIn(savedUser.isLoggedIn());
        response.setPasswordSetDate(savedUser.getPasswordSetDate());
        response.setPassword(savedUser.getPassword());
        response.setEmployeeRollNo(savedUser.getEmployeeRollNo());
        return response;
    }
}
