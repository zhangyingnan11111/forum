package com.forum.aspect;

import com.forum.annotation.RequireRole;
import com.forum.common.exception.BusinessException;
import com.forum.common.exception.ErrorCode;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    private final UserMapper userMapper;

    @Pointcut("@annotation(requireRole)")
    public void rolePointcut(RequireRole requireRole) {
    }

    @Before(value = "rolePointcut(requireRole)", argNames = "requireRole")
    public void checkRole(RequireRole requireRole) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("无法获取请求信息");
        }

        HttpServletRequest request = attributes.getRequest();
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null && requireRole.loginRequired()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (userId != null && requireRole.value().length > 0) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            boolean hasRole = Arrays.stream(requireRole.value())
                    .anyMatch(role -> role.equalsIgnoreCase(user.getRole()));

            if (!hasRole) {
                log.warn("权限不足: userId={}, role={}, requiredRoles={}",
                        userId, user.getRole(), Arrays.toString(requireRole.value()));
                throw new BusinessException(ErrorCode.USER_NO_PERMISSION);
            }
        }
    }
}