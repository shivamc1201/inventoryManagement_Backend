package com.nector.userservice.interceptors.complaint;

import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.interceptors.complaint.model.ComplaintCreateRequest;
import com.nector.userservice.interceptors.complaint.model.ComplaintResponse;
import com.nector.userservice.interceptors.complaint.model.ComplaintStatusUpdateRequest;
import com.nector.userservice.interceptors.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint", description = "APIs for complaint management")
public class ComplaintController {
    
    private final ComplaintService complaintService;
    
    @PostMapping("/create")
    @Operation(summary = "Create complaint", description = "Creates a new complaint")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Complaint created successfully")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Valid @RequestBody ComplaintCreateRequest request) {
        ComplaintResponse response = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully with ID: " + response.getId(), response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID", description = "Retrieves a complaint by its ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint retrieved successfully")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Operation(summary = "Get all complaints", description = "Retrieves all complaints with pagination")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaints retrieved successfully")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplaintResponse> complaints = complaintService.getAllComplaints(pageable);
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update complaint status", description = "Updates the status of a complaint")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint status updated successfully")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }
}