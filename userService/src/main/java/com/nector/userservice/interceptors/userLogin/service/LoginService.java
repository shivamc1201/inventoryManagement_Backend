package com.nector.userservice.interceptors.userLogin.service;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.dto.UserDetailsDTO;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;

public interface LoginService {
    BaseLoginResponse authenticate(LoginRequest request);
    LoginResponse authenticateSecondUser(LoginRequest request);

    UserDetailsDTO getUserDetailsByUsername(String username);
}
