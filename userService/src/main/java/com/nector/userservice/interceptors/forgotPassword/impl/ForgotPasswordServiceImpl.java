package com.nector.userservice.interceptors.forgotPassword.impl;

import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordRequest;
import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordResponse;
import com.nector.userservice.interceptors.forgotPassword.service.ForgotPasswordService;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    
    private final UserRepository userRepository;
    
    @Override
    public ForgotPasswordResponse processForgotPassword(String username, ForgotPasswordRequest request) {
        log.info("Processing forgot password for username: {}", username);
        
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Username not found"));
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        
        if (user.getPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password cannot be same as old password");
        }
        
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        
        log.info("Password updated successfully for username: {}", username);
        
        return new ForgotPasswordResponse(
            "Password updated successfully",
            username,
            "SUCCESS"
        );
    }
}