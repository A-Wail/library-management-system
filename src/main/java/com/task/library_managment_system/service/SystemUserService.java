package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.systemUser.AuthenticationRequest;
import com.task.library_managment_system.dto.systemUser.AuthenticationResp;
import com.task.library_managment_system.dto.systemUser.RequestUser;
import com.task.library_managment_system.dto.systemUser.UserResponse;

import java.util.List;

public interface SystemUserService {
    public AuthenticationResp register(RequestUser request);
    public AuthenticationResp authenticate(AuthenticationRequest request);
    UserResponse createUser(RequestUser requestUser);
    List<UserResponse> getAllUsers();
    UserResponse returnUserById(Long userId);
    UserResponse updateUser(Long userId, RequestUser requestUser);
    void deleteUser(Long userId);
}
