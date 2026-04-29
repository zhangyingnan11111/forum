package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.dto.request.NotificationQueryRequest;
import com.forum.dto.response.NotificationResponse;
import com.forum.entity.Notification;
import com.forum.entity.User;
import com.forum.mapper.NotificationMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public Page<NotificationResponse> getNotifications(Long userId, NotificationQueryRequest request) {
        Page<Notification> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId);

        if (request.getUnreadOnly()) {
            wrapper.eq(Notification::getIsRead, 0);
        }

        wrapper.orderByDesc(Notification::getCreateTime);

        Page<Notification> notificationPage = notificationMapper.selectPage(page, wrapper);

        List<NotificationResponse> records = notificationPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Page<NotificationResponse> responsePage = new Page<>(
                notificationPage.getCurrent(),
                notificationPage.getSize(),
                notificationPage.getTotal()
        );
        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getReceiverId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    /**
     * 转换为响应对象
     */
    private NotificationResponse convertToResponse(Notification notification) {
        User sender = userMapper.selectById(notification.getSenderId());

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .senderName(sender != null ? sender.getUsername() : "未知用户")
                .senderId(notification.getSenderId())
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .content(notification.getContent())
                .isRead(notification.getIsRead() == 1)
                .createTime(notification.getCreateTime())
                .build();
    }
}