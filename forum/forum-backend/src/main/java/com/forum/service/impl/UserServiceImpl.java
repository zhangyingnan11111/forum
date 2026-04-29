package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.common.exception.BusinessException;
import com.forum.common.exception.ErrorCode;
import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.request.UserUpdateRequest;
import com.forum.dto.response.LoginResponse;
import com.forum.dto.response.UserInfoResponse;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.service.UserService;
import com.forum.utils.JwtUtil;
import com.forum.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        // 检查邮箱是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS.getCode(), "邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus(1);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("注册失败，请稍后重试");
        }

        log.info("用户注册成功: username={}, email={}", user.getUsername(), user.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0);
        User user = userMapper.selectOne(wrapper);

        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 密码错误
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        // 账号被禁用
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 更新登录信息
        userMapper.updateLoginInfo(user.getId(), ip);

        log.info("用户登录成功: userId={}, username={}, ip={}", user.getId(), user.getUsername(), ip);

        // 返回登录响应
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .token(token)
                .expiresIn(86400000L)  // 24小时
                .build();
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = getUserById(userId);

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .intro(user.getIntro())
                .role(user.getRole())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        // 更新用户名（如果改变且新用户名未被占用）
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS.getCode(), "用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 更新邮箱（如果改变且新邮箱未被占用）
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS.getCode(), "邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }

        // 更新其他字段
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getIntro() != null) {
            user.setIntro(request.getIntro());
        }

        userMapper.updateById(user);

        log.info("用户信息更新成功: userId={}", userId);

        return getUserInfo(userId);
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}