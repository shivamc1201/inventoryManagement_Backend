package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String sessionToken;
    
    private String deviceInfo;
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime loginTime;
    
    private LocalDateTime lastActivity;
    
    @Column(nullable = false)
    private boolean active = true;
}