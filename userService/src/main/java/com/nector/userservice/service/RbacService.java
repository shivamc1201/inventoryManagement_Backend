package com.nector.userservice.service;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Set<Features> getCurrentUserFeatures() {
        // For testing without authentication, return features for user ID 1
        // Uncomment below for real authentication
        /*
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Set.of();
        }
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        return getUserFeatures(principal.getUserId());
        */
        return getUserFeatures(1L);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUserPermissionsWithStatus() {
        // For testing without authentication, use user ID 1
        return getUserPermissionsWithStatus(1L);
    }
    
    @Transactional(readOnly = true)
    public Set<Features> getUserFeatures(Long userId) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getFeature())
            .collect(Collectors.toSet());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPermissionsWithStatus(Long userId) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active. Status: " + user.getStatus());
        }
        
        Set<Features> features = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getFeature())
            .collect(Collectors.toSet());
        
        return Map.of(
            "userId", user.getId(),
            "status", user.getStatus().name(),
            "roleType", "SuperAdmin",
            "features", features.stream()
                .map(feature -> Map.of(
                    "name", feature.name(),
                    "displayName", feature.getDisplayName(),
                    "path", feature.getPath()
                ))
                .collect(Collectors.toList()),
            "featureNames", features.stream().map(Features::name).collect(Collectors.toSet())
        );
    }
    
    @Transactional(readOnly = true)
    public boolean hasFeature(Long userId, Features feature) {
        return getUserFeatures(userId).contains(feature);
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserHasFeature(Features feature) {
        return getCurrentUserFeatures().contains(feature);
    }
}