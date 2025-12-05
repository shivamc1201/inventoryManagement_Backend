package com.nector.userservice.interceptors.userCreate.model;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "User registration response")
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private String firstName;
    private String lastName;
    private LocalDateTime createdOn;
    private RoleType roleType;
}