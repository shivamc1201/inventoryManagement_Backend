package com.nector.userservice.interceptors.userLogin.service;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.dto.UserDetailsDTO;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.model.UnifiedLoginResponse;

public interface LoginService {
    BaseLoginResponse authenticate(LoginRequest request);
    LoginResponse authenticateSecondUser(LoginRequest request);
    BaseLoginResponse authenticateWithPermissions(LoginRequest request);
    UserDetailsDTO getUserDetailsByUsername(String username);
}
