package com.nector.userservice.interceptors.userLogin.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.service.LoginService;
import com.nector.userservice.model.User;
// import com.nector.userservice.model.UserSession;
import com.nector.userservice.repository.UserRepository;
// import com.nector.userservice.repository.UserSessionRepository;
import com.nector.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
    
    private final UserRepository userRepository;
    // private final UserSessionRepository userSessionRepository; // For future session management
    private final JwtService jwtService;
    
    @Override
    // @Transactional // For future session management
    public LoginResponse authenticateUser(LoginRequest request) {
        log.info("Entering authenticateUser() for username: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);
        
        if (user == null) {
            log.warn("Exiting authenticateUser() - Username not available: {}", request.getUsername());
            throw new RuntimeException("Username not available");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Exiting authenticateUser() - Username inactive: {}", request.getUsername());
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }
        
        // Force logout previous session if user is already logged in
        if (user.isLoggedIn()) {
            log.warn("User already logged in, forcing logout previous session: {}", request.getUsername());
            user.setLoggedIn(false);
        }
        
        if (!user.getPassword().equals(request.getPassword())) {
            log.warn("Exiting authenticateUser() - Invalid password for username: {}", request.getUsername());
            throw new RuntimeException("Invalid password");
        }
        
        // Get user with roles and permissions
        user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
            .orElse(user);
        
        String token = jwtService.generateToken(request.getUsername());
        
        /* FUTURE SESSION MANAGEMENT CODE - UNCOMMENT WHEN NEEDED
        // Create new session
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setSessionToken(token);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        userSessionRepository.save(session);
        */
        
        // Update user login status and last login time
        user.setLoggedIn(true);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        Set<Features> features;
        if (user.getRoleType() == com.nector.userservice.common.RoleType.SUPER_ADMIN) {
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
        
        LoginResponse response = new LoginResponse(
            token,
            "Bearer",
            request.getUsername(),
            "Login successful for " + request.getUsername(),
            user.getRoleType().name(),
            user.getId(),
            featureDetails,
            featureNames,
            "LOGGED_IN"
        );
        
        log.info("Exiting authenticateUser() - Login successful for username: {}", request.getUsername());
        return response;
    }
}