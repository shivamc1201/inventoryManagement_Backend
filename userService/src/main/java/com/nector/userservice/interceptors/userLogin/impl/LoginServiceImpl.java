package com.nector.userservice.interceptors.userLogin.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.service.LoginService;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Override
    public LoginResponse authenticateUser(LoginRequest request) {
        log.error("Login attempt - Username: {}, Password: {}", request.getUsername(), request.getPassword());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);
        
        if (user == null) {
            throw new RuntimeException("Username not available");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }
        
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Get user with roles and permissions for active users
        user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
            .orElse(user);
        
        Set<Features> features;
        if (user.getRoleType() == com.nector.userservice.common.RoleType.SUPER_ADMIN) {
            // Super admin gets all features
            features = Set.of(Features.values());
        } else {
            features = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getFeature())
                .collect(Collectors.toSet());
        }
        
        List<Object> featureDetails = features.stream()
            .map(feature -> Map.of(
                "name", feature.name(),
                "displayName", feature.getDisplayName(),
                "path", feature.getPath()
            ))
            .collect(Collectors.toList());
        
        Set<String> featureNames = features.stream()
            .map(Features::name)
            .collect(Collectors.toSet());
        
        String token = jwtService.generateToken(request.getUsername());
        
        return new LoginResponse(
            token,
            "Bearer",
            request.getUsername(),
            "Login successful for" + request.getUsername(),
            user.getRoleType().name(),
            user.getId(),
            featureDetails,
            featureNames
        );
    }
}