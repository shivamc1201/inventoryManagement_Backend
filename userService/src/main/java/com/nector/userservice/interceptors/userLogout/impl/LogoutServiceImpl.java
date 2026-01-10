package com.nector.userservice.interceptors.userLogout.impl;

import com.nector.userservice.interceptors.userLogout.model.LogoutRequest;
import com.nector.userservice.interceptors.userLogout.model.LogoutResponse;
import com.nector.userservice.interceptors.userLogout.service.LogoutService;
import com.nector.userservice.model.User;
// import com.nector.userservice.model.UserSession;
import com.nector.userservice.repository.UserRepository;
// import com.nector.userservice.repository.UserSessionRepository;
import com.nector.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {
    
    private final UserRepository userRepository;
    // private final UserSessionRepository userSessionRepository; // For future session management
    private final JwtService jwtService;
    
    @Override
    // @Transactional // For future session management
    public LogoutResponse logoutUser(LogoutRequest request) {
        log.info("Entering logoutUser() for token");
        
        // Current implementation - expects username in token field
        String username = request.getToken();
        if (username == null || username.trim().isEmpty()) {
            log.warn("Exiting logoutUser() - Invalid User");
            throw new RuntimeException("Invalid user");
        }
        
        /* FUTURE SESSION MANAGEMENT CODE - UNCOMMENT WHEN NEEDED
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            log.warn("Exiting logoutUser() - Invalid token");
            throw new RuntimeException("Invalid token");
        }
        
        // Find and deactivate session
        UserSession session = userSessionRepository.findBySessionTokenAndActiveTrue(token)
            .orElseThrow(() -> new RuntimeException("Session not found or already expired"));
        
        session.setActive(false);
        userSessionRepository.save(session);
        
        // Check if user has any other active sessions
        boolean hasActiveSessions = !userSessionRepository.findByUserIdAndActiveTrue(session.getUserId()).isEmpty();
        
        // Update user login status only if no other active sessions
        if (!hasActiveSessions) {
            User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setLoggedIn(false);
            userRepository.save(user);
        }
        */
        
        // Current force logout implementation
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setLoggedIn(false);
        userRepository.save(user);
        
        LogoutResponse response = new LogoutResponse("Logout successful", "LOGGED_OUT");
        
        log.info("Exiting logoutUser() - Logout successful for username: {}", username);
        return response;
    }
}