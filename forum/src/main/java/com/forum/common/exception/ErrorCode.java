package com.forum.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 用户相关错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_NOT_LOGIN(1004, "请先登录"),
    USER_TOKEN_EXPIRED(1005, "Token已过期"),
    USER_NO_PERMISSION(1006, "没有操作权限"),

    // 参数错误
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "缺少必要参数"),

    // 系统错误
    SYSTEM_ERROR(5001, "系统繁忙，请稍后再试"),
    DB_ERROR(5002, "数据库操作失败"),
    REDIS_ERROR(5003, "缓存服务异常");

    private final Integer code;
    private final String msg;

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}