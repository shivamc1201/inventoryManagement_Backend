package com.nector.userservice.interceptors.hrMaster.service;

import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginResponse;
import com.nector.userservice.model.UserApproval;
import java.util.List;

public interface HrMasterService {
    List<UserApproval> getPendingApprovals();
    String processApproval(ApprovalRequest request, String hrMasterUsername);
    HrMasterLoginResponse login(HrMasterLoginRequest request);
}