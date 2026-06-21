package com.nector.userservice.interceptors.hrMaster.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.RoleType;
import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginResponse;
import com.nector.userservice.interceptors.hrMaster.service.HrMasterService;
import com.nector.userservice.model.HrMaster;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
import com.nector.userservice.repository.HrMasterRepository;
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
public class HrMasterServiceImpl implements HrMasterService {
    
    private final UserApprovalRepository userApprovalRepository;
    private final UserRepository userRepository;
    private final HrMasterRepository hrMasterRepository;
    
    @Override
    public List<UserApproval> getPendingApprovals() {
        log.info("Entering getPendingApprovals()");
        List<UserApproval> result = userApprovalRepository.findByApprovalStatus("PENDING");
        log.info("Exiting getPendingApprovals() with {} pending approvals", result.size());
        return result;
    }
    
    @Override
    public String processApproval(ApprovalRequest request, String hrMasterUsername) {
        log.info("Entering processApproval() with userId: {}, action: {}, hrMaster: {}", 
                request.getUserId(), request.getAction(), hrMasterUsername);
        
        UserApproval approval = userApprovalRepository.findByUserId(request.getUserId());
        if (approval == null) {
            log.warn("Exiting processApproval() - Approval request not found for userId: {}", request.getUserId());
            return "Approval request not found";
        }
        
        HrMaster hrMaster = hrMasterRepository.findByUsername(hrMasterUsername);
        if (hrMaster == null) {
            log.warn("Exiting processApproval() - HrMaster not found: {}", hrMasterUsername);
            return "HrMaster not found";
        }
        
        User user = approval.getUser();
        
        if ("APPROVE".equals(request.getAction())) {
            user.setStatus(UserStatus.ACTIVE);
            approval.setApprovalStatus("APPROVED");
        } else if ("REJECT".equals(request.getAction())) {
            user.setStatus(UserStatus.INACTIVE);
            approval.setApprovalStatus("REJECTED");
        }
        
        approval.setReviewedBy(hrMaster);
        approval.setReviewedOn(LocalDateTime.now());
        approval.setReviewComments(request.getComments());
        
        userRepository.save(user);
        userApprovalRepository.save(approval);
        
        String result = "User " + request.getAction().toLowerCase() + "d successfully";
        log.info("Exiting processApproval() with result: {}", result);
        return result;
    }
    
    @Override
    public HrMasterLoginResponse login(HrMasterLoginRequest request) {
        log.info("Entering login() for username: {}", request.getUsername());
        
        HrMaster hrMaster = hrMasterRepository.findByUsername(request.getUsername());
        HrMasterLoginResponse response = new HrMasterLoginResponse();
        
        if (hrMaster != null && hrMaster.getPassword().equals(request.getPassword())) {
            hrMaster.setLastLoginTime(LocalDateTime.now());
            hrMasterRepository.save(hrMaster);
            
            response.setId(hrMaster.getId());
            response.setUsername(hrMaster.getUsername());
            response.setEmail(hrMaster.getEmail());
            response.setFirstName(hrMaster.getFirstName());
            response.setLastName(hrMaster.getLastName());
            response.setMessage("Login successful");
            log.info("Exiting login() - Login successful for username: {}", request.getUsername());
        } else {
            response.setMessage("Invalid credentials");
            log.warn("Exiting login() - Invalid credentials for username: {}", request.getUsername());
        }
        
        return response;
    }
    
    @Override
    public List<User> getAllSalespersons() {
        log.info("Entering getAllSalespersons()");
        List<User> salespersons = userRepository.findByRoleTypeIn(List.of(
            RoleType.NATIONAL_SALES_MGR,
            RoleType.STATE_SALES_MGR,
            RoleType.ZONAL_SALES_MGR,
            RoleType.REGIONAL_SALES_MGR,
            RoleType.AREA_SALES_MGR,
            RoleType.SALES_OFFICER,
            RoleType.SALES_EXECUTIVE
        ));
        log.info("Exiting getAllSalespersons() with {} salespersons found", salespersons.size());
        return salespersons;
    }
}