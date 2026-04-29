package com.forum.interceptor;

import com.forum.annotation.LoginRequired;
import com.forum.common.exception.BusinessException;
import com.forum.common.exception.ErrorCode;
import com.forum.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    // 白名单路径（不需要登录）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/login",
            "/user/register",
            "/test/ping",
            "/test/need-auth",
            "/error",
            "/favicon.ico"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // 白名单检查
        if (isWhiteList(path)) {
            log.debug("白名单路径，放行: {}", path);
            return true;
        }

        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查是否需要登录
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            loginRequired = handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
        }

        // 如果不需要登录，直接放行
        if (loginRequired == null || !loginRequired.required()) {
            return true;
        }

        // 获取token
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("未登录访问受保护资源: {}", path);
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        // 将用户信息存入请求属性中
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        log.debug("用户认证通过: userId={}, username={}, path={}", userId, username, path);
        return true;
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}