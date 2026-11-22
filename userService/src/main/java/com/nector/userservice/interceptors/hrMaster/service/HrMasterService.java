package com.nector.userservice.interceptors.hrMaster.service;

import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.PendingUserEditRequest;
import com.nector.userservice.model.PendingOnboardingRequest;
import com.nector.userservice.model.User;

import java.util.List;

public interface HrMasterService {
    List<PendingOnboardingRequest> getPendingApprovals();
    String processApproval(ApprovalRequest request, String reviewedByUsername);
    List<User> getAllSalespersons();
    PendingOnboardingRequest editPendingUser(Long pendingRequestId, PendingUserEditRequest request);
}
