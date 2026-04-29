package com.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String avatar;

    private String intro;

    private String role;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /**
     * 是否是管理员
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /**
     * 是否是版主
     */
    public boolean isModerator() {
        return "moderator".equals(role);
    }

    /**
     * 是否是普通用户
     */
    public boolean isUser() {
        return "user".equals(role);
    }

    /**
     * 是否有管理权限（管理员或版主）
     */
    public boolean hasManagePermission() {
        return isAdmin() || isModerator();
    }
}