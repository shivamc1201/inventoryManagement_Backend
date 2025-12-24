package com.nector.userservice.interceptors.superAdmin.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.interceptors.superAdmin.model.ApprovalRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginResponse;
import com.nector.userservice.interceptors.superAdmin.service.SuperAdminService;
import com.nector.userservice.model.SuperAdmin;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
import com.nector.userservice.repository.SuperAdminRepository;
import com.nector.userservice.repository.UserApprovalRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminServiceImpl implements SuperAdminService {
    
    private final UserApprovalRepository userApprovalRepository;
    private final UserRepository userRepository;
    private final SuperAdminRepository superAdminRepository;
    
    @Override
    public List<UserApproval> getPendingApprovals() {
        log.info("Entering getPendingApprovals()");
        List<UserApproval> result = userApprovalRepository.findByApprovalStatus("PENDING");
        log.info("Exiting getPendingApprovals() with {} pending approvals", result.size());
        return result;
    }
    
    @Override
    public String processApproval(ApprovalRequest request, String superAdminUsername) {
        log.info("Entering processApproval() with userId: {}, action: {}, superAdmin: {}", 
                request.getUserId(), request.getAction(), superAdminUsername);
        
        UserApproval approval = userApprovalRepository.findByUserId(request.getUserId());
        if (approval == null) {
            log.warn("Exiting processApproval() - Approval request not found for userId: {}", request.getUserId());
            return "Approval request not found";
        }
        
        SuperAdmin superAdmin = superAdminRepository.findByUsername(superAdminUsername);
        if (superAdmin == null) {
            log.warn("Exiting processApproval() - SuperAdmin not found: {}", superAdminUsername);
            return "SuperAdmin not found";
        }
        
        User user = approval.getUser();
        
        if ("APPROVE".equals(request.getAction())) {
            user.setStatus(UserStatus.ACTIVE);
            approval.setApprovalStatus("APPROVED");
        } else if ("REJECT".equals(request.getAction())) {
            user.setStatus(UserStatus.INACTIVE);
            approval.setApprovalStatus("REJECTED");
        }
        
        approval.setReviewedBy(superAdmin);
        approval.setReviewedOn(LocalDateTime.now());
        approval.setReviewComments(request.getComments());
        
        userRepository.save(user);
        userApprovalRepository.save(approval);
        
        String result = "User " + request.getAction().toLowerCase() + "d successfully";
        log.info("Exiting processApproval() with result: {}", result);
        return result;
    }
    
    @Override
    public SuperAdminLoginResponse login(SuperAdminLoginRequest request) {
        log.info("Entering login() for username: {}", request.getUsername());
        
        SuperAdmin superAdmin = superAdminRepository.findByUsername(request.getUsername());
        SuperAdminLoginResponse response = new SuperAdminLoginResponse();
        
        if (superAdmin != null && superAdmin.getPassword().equals(request.getPassword())) {
            superAdmin.setLastLoginTime(LocalDateTime.now());
            superAdminRepository.save(superAdmin);
            
            response.setId(superAdmin.getId());
            response.setUsername(superAdmin.getUsername());
            response.setEmail(superAdmin.getEmail());
            response.setFirstName(superAdmin.getFirstName());
            response.setLastName(superAdmin.getLastName());
            response.setMessage("Login successful");
            log.info("Exiting login() - Login successful for username: {}", request.getUsername());
        } else {
            response.setMessage("Invalid credentials");
            log.warn("Exiting login() - Invalid credentials for username: {}", request.getUsername());
        }
        
        return response;
    }
}