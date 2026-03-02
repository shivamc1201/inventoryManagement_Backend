package com.nector.userservice.interceptors.forgotPassword.service;

import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordRequest;
import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordResponse;

public interface ForgotPasswordService {
    ForgotPasswordResponse processForgotPassword(String username, ForgotPasswordRequest request);
}