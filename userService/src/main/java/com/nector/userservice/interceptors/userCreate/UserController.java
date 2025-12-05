package com.nector.userservice.interceptors.userCreate;

import com.nector.userservice.interceptors.userCreate.model.UserRequest;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.interceptors.userCreate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/createUsers")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user operations")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create new user", description = "Register a new user in the system")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.registerNewUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}