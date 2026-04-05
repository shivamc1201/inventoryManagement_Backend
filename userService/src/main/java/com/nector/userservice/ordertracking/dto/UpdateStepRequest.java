package com.nector.userservice.ordertracking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStepRequest {
    private String status;   // "completed" | "cancelled" | "pending" | "in-progress"
    private String date;     // "yyyy-MM-dd" or null
    private String remarks;
    
    // Assigned person information
    private Long assignedPersonId;
    private String assignedPersonName;
    private String assignedPersonRole;
    private String assignedPersonPhone;
    private String assignedPersonEmail;
    
    // Document information
    private boolean hasDownload;
    private String downloadLabel;
    private String documentPath;
    
    // Action response (for step 11)
    private boolean hasAction;
    private String actionResponse; // "yes" | "no" | null
}
