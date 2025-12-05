package com.nector.userservice.controller;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
// @PreAuthorize("hasAuthority('FEATURE_DASHBOARD')")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/analytics")
    public ResponseEntity<DashboardResponse> getDashboardAnalytics(
            @RequestParam(defaultValue = "month") String period) {
        DashboardResponse response = dashboardService.getDashboardData(period);
        return ResponseEntity.ok(response);
    }
}