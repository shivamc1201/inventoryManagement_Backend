package com.nector.userservice.interceptors.complaint.service;

import com.nector.userservice.interceptors.complaint.model.ComplaintCreateRequest;
import com.nector.userservice.interceptors.complaint.model.ComplaintResponse;
import com.nector.userservice.interceptors.complaint.model.ComplaintStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {
    
    ComplaintResponse createComplaint(ComplaintCreateRequest request);
    
    ComplaintResponse getComplaintById(Long id);
    
    Page<ComplaintResponse> getAllComplaints(Pageable pageable);
    
    ComplaintResponse updateComplaintStatus(Long id, ComplaintStatusUpdateRequest request);
}