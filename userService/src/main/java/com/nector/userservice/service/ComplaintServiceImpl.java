package com.nector.userservice.service;

import com.nector.userservice.dto.complaint.ComplaintCreateRequest;
import com.nector.userservice.dto.complaint.ComplaintResponse;
import com.nector.userservice.dto.complaint.ComplaintStatusUpdateRequest;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.model.ComplaintEntity;
import com.nector.userservice.model.ComplaintStatus;
import com.nector.userservice.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintServiceImpl implements ComplaintService {
    
    private final ComplaintRepository complaintRepository;
    
    @Override
    public ComplaintResponse createComplaint(ComplaintCreateRequest request) {
        ComplaintEntity complaint = new ComplaintEntity();
        complaint.setType(request.getType());
        complaint.setFullName(request.getFullName());
        complaint.setEmailAddress(request.getEmailAddress());
        complaint.setPhoneNumber(request.getPhoneNumber());
        complaint.setCategory(request.getCategory());
        complaint.setSubject(request.getSubject());
        complaint.setPriorityLevel(request.getPriorityLevel());
        complaint.setDescription(request.getDescription());
        complaint.setStatus(ComplaintStatus.OPEN);
        
        ComplaintEntity savedComplaint = complaintRepository.save(complaint);
        return mapToResponse(savedComplaint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        ComplaintEntity complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return mapToResponse(complaint);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        Page<ComplaintEntity> complaints = complaintRepository.findAll(pageable);
        return complaints.map(this::mapToResponse);
    }
    
    @Override
    public ComplaintResponse updateComplaintStatus(Long id, ComplaintStatusUpdateRequest request) {
        ComplaintEntity complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        
        complaint.setStatus(request.getStatus());
        ComplaintEntity updatedComplaint = complaintRepository.save(complaint);
        return mapToResponse(updatedComplaint);
    }
    
    private ComplaintResponse mapToResponse(ComplaintEntity complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getType(),
                complaint.getFullName(),
                complaint.getEmailAddress(),
                complaint.getPhoneNumber(),
                complaint.getCategory(),
                complaint.getSubject(),
                complaint.getPriorityLevel(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }
}