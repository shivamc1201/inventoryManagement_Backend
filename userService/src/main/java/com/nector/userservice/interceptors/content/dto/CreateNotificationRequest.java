package com.nector.userservice.interceptors.content.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNotificationRequest {
    private String title;
    private String message;
    private String type; // INFO, WARNING, IMPORTANT
    private LocalDateTime expiresAt;
    private String createdBy;
}
