package com.forum.service;

import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.request.UserUpdateRequest;
import com.forum.dto.response.LoginResponse;
import com.forum.dto.response.UserInfoResponse;
import com.forum.entity.User;

public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request);

    /**
     * 根据ID获取用户实体
     */
    User getUserById(Long userId);
}