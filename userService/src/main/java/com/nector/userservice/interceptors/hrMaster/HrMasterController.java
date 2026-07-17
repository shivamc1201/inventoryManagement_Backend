package com.nector.userservice.interceptors.hrMaster;

import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.PendingUserEditRequest;
import com.nector.userservice.interceptors.hrMaster.service.HrMasterService;
import com.nector.userservice.model.PendingOnboardingRequest;
import com.nector.userservice.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hrmaster")
@RequiredArgsConstructor
@Tag(name = "HR Master", description = "APIs for pending onboarding approvals")
public class HrMasterController {

    private final HrMasterService hrMasterService;

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Returns all pending onboarding requests (USER and SALES both)")
    @ApiResponse(responseCode = "200", description = "Pending approvals retrieved successfully")
    public ResponseEntity<List<PendingOnboardingRequest>> getPendingApprovals() {
        return ResponseEntity.ok(hrMasterService.getPendingApprovals());
    }

    @PostMapping("/process-approval")
    @Operation(summary = "Approve or reject a pending request",
               description = "APPROVE saves to users or sales_persons table. REJECT discards. Pass current user's username in header for tracking.")
    @ApiResponse(responseCode = "200", description = "Approval processed successfully")
    public ResponseEntity<String> processApproval(
            @RequestBody ApprovalRequest request,
            @RequestHeader("HrMaster-Username") String reviewedByUsername) {
        return ResponseEntity.ok(hrMasterService.processApproval(request, reviewedByUsername));
    }

    @PatchMapping("/edit-pending/{pendingRequestId}")
    @Operation(summary = "Edit a pending request", description = "Correct wrong data before approving. Send only fields to fix.")
    @ApiResponse(responseCode = "200", description = "Request updated successfully")
    public ResponseEntity<PendingOnboardingRequest> editPendingUser(
            @PathVariable Long pendingRequestId,
            @RequestBody PendingUserEditRequest request) {
        return ResponseEntity.ok(hrMasterService.editPendingUser(pendingRequestId, request));
    }

    @GetMapping("/salespersons")
    @Operation(summary = "Get all approved salespersons")
    @ApiResponse(responseCode = "200", description = "Salespersons retrieved successfully")
    public ResponseEntity<List<User>> getAllSalespersons() {
        return ResponseEntity.ok(hrMasterService.getAllSalespersons());
    }
}
