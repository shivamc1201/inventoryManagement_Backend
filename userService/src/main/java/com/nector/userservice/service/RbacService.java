package com.nector.userservice.service;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbacService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Set<Features> getCurrentUserFeatures() {
        log.info("Entering getCurrentUserFeatures()");
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
        Set<Features> features = getUserFeatures(1L);
        log.info("Exiting getCurrentUserFeatures() with {} features", features.size());
        return features;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUserPermissionsWithStatus() {
        log.info("Entering getCurrentUserPermissionsWithStatus()");
        // For testing without authentication, use user ID 1
        Map<String, Object> permissions = getUserPermissionsWithStatus(1L);
        log.info("Exiting getCurrentUserPermissionsWithStatus()");
        return permissions;
    }
    
    @Transactional(readOnly = true)
    public Set<Features> getUserFeatures(Long userId) {
        log.info("Entering getUserFeatures() for userId: {}", userId);
        
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
            .orElseThrow(() -> {
                log.warn("Exiting getUserFeatures() - User not found: {}", userId);
                return new RuntimeException("User not found");
            });
        
        Set<Features> features = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getFeature())
            .collect(Collectors.toSet());
        
        log.info("Exiting getUserFeatures() with {} features for userId: {}", features.size(), userId);
        return features;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPermissionsWithStatus(Long userId) {
        log.info("Entering getUserPermissionsWithStatus() for userId: {}", userId);
        
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
            .orElseThrow(() -> {
                log.warn("Exiting getUserPermissionsWithStatus() - User not found: {}", userId);
                return new RuntimeException("User not found");
            });
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Exiting getUserPermissionsWithStatus() - User account not active: {}", user.getStatus());
            throw new RuntimeException("User account is not active. Status: " + user.getStatus());
        }
        
        Set<Features> features = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getFeature())
            .collect(Collectors.toSet());
        
        Map<String, Object> result = Map.of(
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
        
        log.info("Exiting getUserPermissionsWithStatus() for userId: {}", userId);
        return result;
    }
    
    @Transactional(readOnly = true)
    public boolean hasFeature(Long userId, Features feature) {
        log.debug("Entering hasFeature() for userId: {}, feature: {}", userId, feature);
        boolean hasFeature = getUserFeatures(userId).contains(feature);
        log.debug("Exiting hasFeature() with result: {} for userId: {}, feature: {}", hasFeature, userId, feature);
        return hasFeature;
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserHasFeature(Features feature) {
        log.debug("Entering currentUserHasFeature() for feature: {}", feature);
        boolean hasFeature = getCurrentUserFeatures().contains(feature);
        log.debug("Exiting currentUserHasFeature() with result: {} for feature: {}", hasFeature, feature);
        return hasFeature;
    }
}