package com.nector.userservice.controller;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class UserPermissionController {
    
    private final RbacService rbacService;
    
    @GetMapping("/current-user")
    // @PreAuthorize("isAuthenticated(")
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
    // @PreAuthorize("hasAuthority('FEATURE_USER_RIGHTS')")
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
    // @PreAuthorize("hasAuthority('FEATURE_USER_RIGHTS')")
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
}