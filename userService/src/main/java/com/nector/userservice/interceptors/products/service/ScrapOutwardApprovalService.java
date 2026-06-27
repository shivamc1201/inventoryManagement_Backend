package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.model.ScrapOutwardApproval;

import java.util.List;
import java.util.Map;

public interface ScrapOutwardApprovalService {

    ScrapOutwardApproval createApproval(OutwardItemTransaction transaction);

    List<ScrapOutwardApproval> getPendingApprovals();

    Map<String, Object> processApproval(Long approvalId, String action, String adminUsername, String comments);
}
