package com.nector.userservice.test;

import com.nector.userservice.controller.ComplaintController;
import com.nector.userservice.dto.complaint.ComplaintCreateRequest;
import com.nector.userservice.model.ComplaintCategory;
import com.nector.userservice.model.ComplaintType;
import com.nector.userservice.model.PriorityLevel;
import com.nector.userservice.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Simple compilation test for complaint feature
 */
@Component
public class ComplaintCompilationTest {
    
    @Autowired(required = false)
    private ComplaintController complaintController;
    
    @Autowired(required = false)
    private ComplaintService complaintService;
    
    public void testComplaintCreation() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setType(ComplaintType.COMPLAINT);
        request.setFullName("Test User");
        request.setEmailAddress("test@example.com");
        request.setCategory(ComplaintCategory.TECHNICAL);
        request.setSubject("Test Subject");
        request.setPriorityLevel(PriorityLevel.HIGH);
        request.setDescription("Test Description");
        
        // This is just a compilation test - not meant to be executed
        System.out.println("Complaint feature compiled successfully");
    }
}