package com.nector.userservice.interceptors.superAdmin;

import com.nector.userservice.interceptors.superAdmin.model.ApprovalRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginRequest;
import com.nector.userservice.interceptors.superAdmin.model.SuperAdminLoginResponse;
import com.nector.userservice.interceptors.superAdmin.service.SuperAdminService;
import com.nector.userservice.model.UserApproval;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {
    
    private final SuperAdminService superAdminService;
    
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<UserApproval>> getPendingApprovals() {
        List<UserApproval> pendingApprovals = superAdminService.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }
    
    @PostMapping("/process-approval")
    public ResponseEntity<String> processApproval(
            @RequestBody ApprovalRequest request,
            @RequestHeader("SuperAdmin-Username") String superAdminUsername) {
        String result = superAdminService.processApproval(request, superAdminUsername);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/login")
    public ResponseEntity<SuperAdminLoginResponse> login(@RequestBody SuperAdminLoginRequest request) {
        SuperAdminLoginResponse response = superAdminService.login(request);
        return ResponseEntity.ok(response);
    }
}