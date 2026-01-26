package com.nector.userservice.interceptors.complaint.model;

import com.nector.userservice.model.ComplaintCategory;
import com.nector.userservice.model.ComplaintType;
import com.nector.userservice.model.PriorityLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCreateRequest {
    
    @NotNull(message = "Type is required")
    private ComplaintType type;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;
    
    private String phoneNumber;
    
    @NotNull(message = "Category is required")
    private ComplaintCategory category;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;
    
    @NotBlank(message = "Description is required")
    private String description;
}