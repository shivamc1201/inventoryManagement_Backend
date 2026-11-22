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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hrmaster")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HR Master", description = "APIs for pending onboarding approvals")
public class HrMasterController {

    private final HrMasterService hrMasterService;

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Returns all pending onboarding requests (USER and SALES both)")
    @ApiResponse(responseCode = "200", description = "Pending approvals retrieved successfully")
    public ResponseEntity<List<PendingOnboardingRequest>> getPendingApprovals() {
        log.info("Entering getPendingApprovals()");
        List<PendingOnboardingRequest> approvals = hrMasterService.getPendingApprovals();
        log.info("Exiting getPendingApprovals() - returned {} pending approvals", approvals.size());
        return ResponseEntity.ok(approvals);
    }

    @PostMapping("/process-approval")
    @Operation(summary = "Approve or reject a pending request",
               description = "APPROVE saves to users or sales_persons table. REJECT discards. Pass current user's username in header for tracking.")
    @ApiResponse(responseCode = "200", description = "Approval processed successfully")
    public ResponseEntity<String> processApproval(
            @RequestBody ApprovalRequest request,
            @RequestHeader("HrMaster-Username") String reviewedByUsername) {
        log.info("Entering processApproval() - pendingRequestId: {}, action: {}, reviewedBy: {}", request.getPendingRequestId(), request.getAction(), reviewedByUsername);
        String result = hrMasterService.processApproval(request, reviewedByUsername);
        log.info("Exiting processApproval() - pendingRequestId: {}, result: {}", request.getPendingRequestId(), result);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/edit-pending/{pendingRequestId}")
    @Operation(summary = "Edit a pending request", description = "Correct wrong data before approving. Send only fields to fix.")
    @ApiResponse(responseCode = "200", description = "Request updated successfully")
    public ResponseEntity<PendingOnboardingRequest> editPendingUser(
            @PathVariable Long pendingRequestId,
            @RequestBody PendingUserEditRequest request) {
        log.info("Entering editPendingUser() for pendingRequestId: {}", pendingRequestId);
        PendingOnboardingRequest updated = hrMasterService.editPendingUser(pendingRequestId, request);
        log.info("Exiting editPendingUser() for pendingRequestId: {}", pendingRequestId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/salespersons")
    @Operation(summary = "Get all approved salespersons")
    @ApiResponse(responseCode = "200", description = "Salespersons retrieved successfully")
    public ResponseEntity<List<User>> getAllSalespersons() {
        log.info("Entering getAllSalespersons()");
        List<User> salespersons = hrMasterService.getAllSalespersons();
        log.info("Exiting getAllSalespersons() - returned {} salespersons", salespersons.size());
        return ResponseEntity.ok(salespersons);
    }
}
