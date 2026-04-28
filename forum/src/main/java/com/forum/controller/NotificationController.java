package com.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.annotation.LoginRequired;
import com.forum.common.result.Result;
import com.forum.dto.request.NotificationQueryRequest;
import com.forum.dto.response.NotificationResponse;
import com.forum.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    @LoginRequired
    public Result<Page<NotificationResponse>> getNotifications(NotificationQueryRequest request,
                                                               HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Page<NotificationResponse> notifications = notificationService.getNotifications(userId, request);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @LoginRequired
    public Result<Long> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/read/{notificationId}")
    @LoginRequired
    public Result<Void> markAsRead(@PathVariable Long notificationId,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        notificationService.markAsRead(userId, notificationId);
        return Result.success("标记成功", null);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read/all")
    @LoginRequired
    public Result<Void> markAllAsRead(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success("标记成功", null);
    }
}