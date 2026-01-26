package com.nector.userservice.controller;

import com.nector.userservice.dto.DashboardResponse;
import com.nector.userservice.model.User;
import com.nector.userservice.service.DashboardService;
import com.nector.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for dashboard analytics and metrics")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final UserService userService;
    
    @GetMapping("/analytics")
    @Operation(summary = "Get dashboard analytics", description = "Retrieves dashboard analytics data for specified time period")
    @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully")
    public ResponseEntity<DashboardResponse> getDashboardAnalytics(
            @RequestParam(defaultValue = "month") String period) {
        DashboardResponse response = dashboardService.getDashboardData(period);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/userprofile")
    @Operation(summary = "Get user profile", description = "Retrieves user profile data for the logged-in user")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    public ResponseEntity<User> getUserProfile(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}