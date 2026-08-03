package com.nector.userservice.interceptors.complaint.impl;

import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.complaint.model.ComplaintCreateRequest;
import com.nector.userservice.interceptors.complaint.model.ComplaintResponse;
import com.nector.userservice.interceptors.complaint.model.ComplaintStatusUpdateRequest;
import com.nector.userservice.interceptors.complaint.repository.ComplaintRepository;
import com.nector.userservice.interceptors.complaint.service.ComplaintService;
import com.nector.userservice.model.ComplaintEntity;
import com.nector.userservice.model.ComplaintStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {
    
    private final ComplaintRepository complaintRepository;
    
    @Override
    public ComplaintResponse createComplaint(ComplaintCreateRequest request) {
        log.info("Entering createComplaint() - type: {}, category: {}, salespersonId: {}, distributorId: {}", request.getType(), request.getCategory(), request.getSalespersonId(), request.getDistributorId());
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
        complaint.setSalespersonId(request.getSalespersonId());
        complaint.setDistributorId(request.getDistributorId());

        ComplaintEntity savedComplaint = complaintRepository.save(complaint);
        log.info("Exiting createComplaint() - created complaint with ID: {}", savedComplaint.getId());
        return mapToResponse(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        log.info("Entering getComplaintById() with id: {}", id);
        ComplaintEntity complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        log.info("Exiting getComplaintById() - found complaint id: {}", id);
        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        log.info("Entering getAllComplaints() - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ComplaintEntity> complaints = complaintRepository.findAll(pageable);
        log.info("Exiting getAllComplaints() - returned {} total complaints", complaints.getTotalElements());
        return complaints.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getComplaintsByFilter(Long salespersonId, Long distributorId, Pageable pageable) {
        log.info("Entering getComplaintsByFilter() - salespersonId: {}, distributorId: {}", salespersonId, distributorId);
        Page<ComplaintResponse> result;
        if (salespersonId != null && distributorId != null) {
            result = complaintRepository.findBySalespersonIdAndDistributorId(salespersonId, distributorId, pageable)
                    .map(this::mapToResponse);
        } else if (salespersonId != null) {
            result = complaintRepository.findBySalespersonId(salespersonId, pageable).map(this::mapToResponse);
        } else if (distributorId != null) {
            result = complaintRepository.findByDistributorId(distributorId, pageable).map(this::mapToResponse);
        } else {
            result = complaintRepository.findAll(pageable).map(this::mapToResponse);
        }
        log.info("Exiting getComplaintsByFilter() - returned {} total complaints", result.getTotalElements());
        return result;
    }

    @Override
    public ComplaintResponse updateComplaintStatus(Long id, ComplaintStatusUpdateRequest request) {
        log.info("Entering updateComplaintStatus() - id: {}, newStatus: {}", id, request.getStatus());
        ComplaintEntity complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setStatus(request.getStatus());
        complaint.setComment(request.getComment());
        ComplaintEntity updatedComplaint = complaintRepository.save(complaint);
        log.info("Exiting updateComplaintStatus() - updated complaint id: {} to status: {}", id, updatedComplaint.getStatus());
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
                complaint.getComment(),
                complaint.getSalespersonId(),
                complaint.getDistributorId(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt()
        );
    }
}