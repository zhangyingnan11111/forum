package com.forum.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 需要的角色
     * admin: 管理员
     * moderator: 版主
     * user: 普通用户
     */
    String[] value() default {};

    /**
     * 是否需要登录
     */
    boolean loginRequired() default true;
}