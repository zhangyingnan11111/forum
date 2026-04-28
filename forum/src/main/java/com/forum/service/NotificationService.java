package com.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.dto.request.NotificationQueryRequest;
import com.forum.dto.response.NotificationResponse;

public interface NotificationService {

    /**
     * 获取通知列表
     */
    Page<NotificationResponse> getNotifications(Long userId, NotificationQueryRequest request);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
}