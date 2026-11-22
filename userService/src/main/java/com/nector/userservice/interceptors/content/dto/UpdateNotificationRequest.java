package com.nector.userservice.interceptors.content.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateNotificationRequest {
    private String title;
    private String message;
    private String type;
    private LocalDateTime expiresAt;
}
