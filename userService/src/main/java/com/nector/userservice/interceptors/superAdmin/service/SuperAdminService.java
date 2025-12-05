package com.nector.userservice.interceptors.superAdmin.service;

import com.nector.userservice.interceptors.superAdmin.model.ApprovalRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginResponse;
import com.nector.userservice.model.UserApproval;
import java.util.List;

public interface SuperAdminService {
    List<UserApproval> getPendingApprovals();
    String processApproval(ApprovalRequest request, String superAdminUsername);
    SuperAdminLoginResponse login(SuperAdminLoginRequest request);
}