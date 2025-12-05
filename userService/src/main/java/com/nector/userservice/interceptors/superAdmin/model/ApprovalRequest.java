package com.nector.userservice.interceptors.superAdmin.model;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long userId;
    private String action; // APPROVE or REJECT
    private String comments;
}