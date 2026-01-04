package com.nector.userservice.interceptors.userLogout.impl;

import com.nector.userservice.interceptors.userLogout.model.LogoutRequest;
import com.nector.userservice.interceptors.userLogout.model.LogoutResponse;
import com.nector.userservice.interceptors.userLogout.service.LogoutService;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Override
    public LogoutResponse logoutUser(LogoutRequest request) {
        log.info("Entering logoutUser() for token: {}", request.getToken().substring(0, Math.min(10, request.getToken().length())) + "...");
        
        // Validate JWT token
//        String username = jwtService.extractUsername(request.getToken());
//        if (username == null || username.trim().isEmpty()) {
//            log.warn("Exiting logoutUser() - Invalid token");
//            throw new RuntimeException("Invalid token");
//        }

        String username = request.getToken();
        if (username == null || username.trim().isEmpty()) {
            log.warn("Exiting logoutUser() - Invalid User");
            throw new RuntimeException("Invalid user");
        }
        
        // Find user and update logout status
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setLoggedIn(false);
        userRepository.save(user);
        LogoutResponse response = new LogoutResponse("Logout successful", "LOGGED_OUT");
        
        log.info("Exiting logoutUser() - Logout successful for username: {}", username);
        return response;
    }
}