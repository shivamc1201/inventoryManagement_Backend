package com.nector.userservice.interceptors.complaint.model;

import com.nector.userservice.model.ComplaintCategory;
import com.nector.userservice.model.ComplaintStatus;
import com.nector.userservice.model.ComplaintType;
import com.nector.userservice.model.PriorityLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    
    private Long id;
    private ComplaintType type;
    private String fullName;
    private String emailAddress;
    private String phoneNumber;
    private ComplaintCategory category;
    private String subject;
    private PriorityLevel priorityLevel;
    private String description;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}