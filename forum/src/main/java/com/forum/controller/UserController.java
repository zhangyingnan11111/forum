package com.forum.controller;

import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.request.UserUpdateRequest;
import com.forum.dto.response.LoginResponse;
import com.forum.dto.response.UserInfoResponse;
import com.forum.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户模块根路径
     */
    @GetMapping
    public Result<String> userHome() {
        return Result.success("用户模块 - 请使用具体接口：/user/register, /user/login, /user/profile");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功，请登录", null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse response = userService.login(request, ip);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @LoginRequired
    public Result<UserInfoResponse> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    @LoginRequired
    public Result<UserInfoResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request,
                                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        UserInfoResponse userInfo = userService.updateUserInfo(userId, request);
        return Result.success("更新成功", userInfo);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}