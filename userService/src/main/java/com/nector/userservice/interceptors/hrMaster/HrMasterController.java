package com.nector.userservice.interceptors.hrMaster;

import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginRequest;
import com.nector.userservice.interceptors.hrMaster.model.HrMasterLoginResponse;
import com.nector.userservice.interceptors.hrMaster.service.HrMasterService;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
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
@Tag(name = "HR Master", description = "APIs for HR master operations and user approvals")
public class HrMasterController {
    
    private final HrMasterService hrMasterService;
    
    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Retrieves all pending user approval requests")
    @ApiResponse(responseCode = "200", description = "Pending approvals retrieved successfully")
    public ResponseEntity<List<UserApproval>> getPendingApprovals() {
        List<UserApproval> pendingApprovals = hrMasterService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }
    
    @PostMapping("/process-approval")
    @Operation(summary = "Process user approval", description = "Approves or rejects a user registration request")
    @ApiResponse(responseCode = "200", description = "Approval processed successfully")
    public ResponseEntity<String> processApproval(
            @RequestBody ApprovalRequest request,
            @RequestHeader("HrMaster-Username") String hrMasterUsername) {
        String result = hrMasterService.processApproval(request, hrMasterUsername);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/salespersons")
    @Operation(summary = "Get all salespersons", description = "Retrieves all users with salesperson role")
    @ApiResponse(responseCode = "200", description = "Salespersons retrieved successfully")
    public ResponseEntity<List<User>> getAllSalespersons() {
        List<User> salespersons = hrMasterService.getAllSalespersons();
        return ResponseEntity.ok(salespersons);
    }
}