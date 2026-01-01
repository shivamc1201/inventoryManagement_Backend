package com.nector.userservice.service;

import com.nector.userservice.dto.complaint.ComplaintCreateRequest;
import com.nector.userservice.dto.complaint.ComplaintResponse;
import com.nector.userservice.dto.complaint.ComplaintStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {
    
    ComplaintResponse createComplaint(ComplaintCreateRequest request);
    
    ComplaintResponse getComplaintById(Long id);
    
    Page<ComplaintResponse> getAllComplaints(Pageable pageable);
    
    ComplaintResponse updateComplaintStatus(Long id, ComplaintStatusUpdateRequest request);
}