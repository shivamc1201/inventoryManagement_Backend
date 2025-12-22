package com.nector.userservice.interceptors.superAdmin;

import com.nector.userservice.interceptors.superAdmin.model.ApprovalRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginResponse;
import com.nector.userservice.interceptors.superAdmin.service.SuperAdminService;
import com.nector.userservice.model.UserApproval;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@Tag(name = "Super Admin", description = "APIs for super admin operations and user approvals")
public class SuperAdminController {
    
    private final SuperAdminService superAdminService;
    
    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals", description = "Retrieves all pending user approval requests")
    @ApiResponse(responseCode = "200", description = "Pending approvals retrieved successfully")
    public ResponseEntity<List<UserApproval>> getPendingApprovals() {
        List<UserApproval> pendingApprovals = superAdminService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }
    
    @PostMapping("/process-approval")
    @Operation(summary = "Process user approval", description = "Approves or rejects a user registration request")
    @ApiResponse(responseCode = "200", description = "Approval processed successfully")
    public ResponseEntity<String> processApproval(
            @RequestBody ApprovalRequest request,
            @RequestHeader("SuperAdmin-Username") String superAdminUsername) {
        String result = superAdminService.processApproval(request, superAdminUsername);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Super admin login", description = "Authenticates super admin with credentials")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<SuperAdminLoginResponse> login(@RequestBody SuperAdminLoginRequest request) {
        SuperAdminLoginResponse response = superAdminService.login(request);
        return ResponseEntity.ok(response);
    }
}