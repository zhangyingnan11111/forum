package com.forum.service.impl;

import com.forum.common.exception.BusinessException;
import com.forum.entity.LikeRecord;
import com.forum.entity.Notification;
import com.forum.entity.Post;
import com.forum.entity.Comment;
import com.forum.mapper.*;
import com.forum.service.LikeService;
import com.forum.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long userId, String targetType, Long targetId) {
        // 检查是否已点赞
        LikeRecord existing = likeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException("已经点赞过了");
        }

        // 获取目标对象的所有者
        Long targetOwnerId = getTargetOwnerId(targetType, targetId);

        // 创建或更新点赞记录
        if (existing == null) {
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            record.setStatus(1);
            likeRecordMapper.insert(record);
        } else {
            existing.setStatus(1);
            likeRecordMapper.updateById(existing);
        }

        // 更新目标对象的点赞数
        updateLikeCount(targetType, targetId, 1);

        // 创建通知（如果不是自己点赞自己）
        if (targetOwnerId != null && !targetOwnerId.equals(userId)) {
            String content = getLikeContent(targetType, targetId);
            createNotification(targetOwnerId, userId, "like", targetType, targetId, content);
        }

        log.info("点赞成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long userId, String targetType, Long targetId) {
        LikeRecord record = likeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);
        if (record == null || record.getStatus() == 0) {
            throw new BusinessException("尚未点赞");
        }

        record.setStatus(0);
        likeRecordMapper.updateById(record);

        // 更新目标对象的点赞数
        updateLikeCount(targetType, targetId, -1);

        log.info("取消点赞成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    public boolean getLikeStatus(Long userId, String targetType, Long targetId) {
        LikeRecord record = likeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);
        return record != null && record.getStatus() == 1;
    }

    @Override
    public Integer getLikeCount(String targetType, Long targetId) {
        if ("post".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return post != null ? post.getLikeCount() : 0;
        } else if ("comment".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            return comment != null ? comment.getLikeCount() : 0;
        }
        return 0;
    }

    /**
     * 获取目标对象的所有者ID
     */
    private Long getTargetOwnerId(String targetType, Long targetId) {
        if ("post".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return post != null ? post.getAuthorId() : null;
        } else if ("comment".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            return comment != null ? comment.getAuthorId() : null;
        }
        return null;
    }

    /**
     * 更新点赞数
     */
    private void updateLikeCount(String targetType, Long targetId, int delta) {
        if ("post".equals(targetType)) {
            postMapper.updateLikeCount(targetId, delta);
            redisUtil.delete("post:detail:" + targetId);
        } else if ("comment".equals(targetType)) {
            commentMapper.updateLikeCount(targetId, delta);
        }
    }

    /**
     * 获取点赞通知内容
     */
    private String getLikeContent(String targetType, Long targetId) {
        if ("post".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return "点赞了你的帖子：" + (post != null ? post.getTitle() : "");
        } else if ("comment".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            return "点赞了你的评论：" + (comment != null ? comment.getContent() : "");
        }
        return "点赞了你的内容";
    }

    /**
     * 创建通知
     */
    private void createNotification(Long receiverId, Long senderId, String type,
                                    String targetType, Long targetId, String content) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
    }
}