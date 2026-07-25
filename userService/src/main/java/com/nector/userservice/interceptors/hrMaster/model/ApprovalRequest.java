package com.nector.userservice.interceptors.hrMaster.model;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long pendingRequestId;
    private String action;   // APPROVE or REJECT
    private String comments;
}
