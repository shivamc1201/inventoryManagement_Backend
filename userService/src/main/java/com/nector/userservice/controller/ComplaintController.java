package com.nector.userservice.controller;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.dto.complaint.ComplaintCreateRequest;
import com.nector.userservice.dto.complaint.ComplaintResponse;
import com.nector.userservice.dto.complaint.ComplaintStatusUpdateRequest;
import com.nector.userservice.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {
    
    private final ComplaintService complaintService;
    
    @PostMapping("/create_complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Valid @RequestBody ComplaintCreateRequest request) {
        ComplaintResponse response = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully with ID: " + response.getId(), response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/getAllComplaints")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplaintResponse> complaints = complaintService.getAllComplaints(pageable);
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }
}