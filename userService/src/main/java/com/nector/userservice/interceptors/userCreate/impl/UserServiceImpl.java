package com.nector.userservice.interceptors.userCreate.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.interceptors.userCreate.model.UserRequest;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.exception.UsernameAlreadyExistsException;
import com.nector.userservice.interceptors.userCreate.service.UserService;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.repository.UserApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserApprovalRepository userApprovalRepository;
    
    @Override
    public UserResponse registerNewUser(UserRequest request) throws UsernameAlreadyExistsException {
        log.info("Entering registerNewUser() for username: {}, email: {}", request.getUsername(), request.getEmail());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Exiting registerNewUser() - Username already exists: {}", request.getUsername());
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Exiting registerNewUser() - Email already exists: {}", request.getEmail());
            throw new UsernameAlreadyExistsException("Email already exists: " + request.getEmail());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setStatus(UserStatus.PENDING);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setContactNo(request.getContactNo());
        user.setAlternateContactNo(request.getAlternateContactNo());
        user.setBloodGroup(request.getBloodGroup());
        user.setCompleteAddress(request.getCompleteAddress());
        user.setGender(request.getGender());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setZip(request.getZip());
        user.setRoleType(request.getRoleType());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setOtp("1234"); // Default OTP since OTP service is disabled
        
        User savedUser = userRepository.save(user);
        
        // Create approval request for maker-checker flow
        UserApproval approval = new UserApproval();
        approval.setUser(savedUser);
        userApprovalRepository.save(approval);

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
        
        log.info("Exiting registerNewUser() - User registered successfully with ID: {}", savedUser.getId());
        return response;
    }
}