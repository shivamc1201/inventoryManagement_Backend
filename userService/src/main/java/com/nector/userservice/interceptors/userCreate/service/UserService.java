package com.nector.userservice.interceptors.userCreate.service;

import com.nector.userservice.interceptors.userCreate.model.UserRequest;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.exception.UsernameAlreadyExistsException;

public interface UserService {
    UserResponse registerNewUser(UserRequest request) throws UsernameAlreadyExistsException;
}