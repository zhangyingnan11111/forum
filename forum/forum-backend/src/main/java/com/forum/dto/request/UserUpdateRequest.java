package com.forum.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    @Size(max = 200, message = "个人简介不能超过200字")
    private String intro;
}