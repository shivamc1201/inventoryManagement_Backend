package com.nector.userservice.interceptors.userLogout.service;

import com.nector.userservice.interceptors.userLogout.model.LogoutRequest;
import com.nector.userservice.interceptors.userLogout.model.LogoutResponse;

public interface LogoutService {
    LogoutResponse logoutUser(LogoutRequest request);
}