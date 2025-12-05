package com.nector.userservice.interceptors.userLogin.service;

import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;

public interface LoginService {
    LoginResponse authenticateUser(LoginRequest request);
}