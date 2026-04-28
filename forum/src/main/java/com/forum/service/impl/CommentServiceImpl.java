package com.forum.service.impl;

import com.forum.common.exception.BusinessException;
import com.forum.common.exception.ErrorCode;
import com.forum.dto.request.CommentCreateRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.entity.Comment;
import com.forum.entity.Notification;
import com.forum.entity.Post;
import com.forum.entity.User;
import com.forum.mapper.CommentMapper;
import com.forum.mapper.NotificationMapper;
import com.forum.mapper.PostMapper;
import com.forum.mapper.UserMapper;
import com.forum.service.CommentService;
import com.forum.service.LikeService;
import com.forum.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final LikeService likeService;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, CommentCreateRequest request) {
        // 验证帖子是否存在
        Post post = postMapper.selectById(request.getPostId());
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException("帖子不存在或已被删除");
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPostId(request.getPostId());
        comment.setAuthorId(userId);
        comment.setLikeCount(0);
        comment.setStatus(1);

        int result = commentMapper.insert(comment);
        if (result <= 0) {
            throw new BusinessException("评论失败，请稍后重试");
        }

        // 更新帖子的评论数
        postMapper.updateCommentCount(request.getPostId(), 1);

        // 更新帖子的最后评论时间
        post.setLastCommentTime(java.time.LocalDateTime.now());
        postMapper.updateById(post);

        // 创建通知（如果不是自己评论自己的帖子）
        if (!post.getAuthorId().equals(userId)) {
            createNotification(post.getAuthorId(), userId, "comment", "post",
                    request.getPostId(), "评论了你的帖子：" + request.getContent());
        }

        // 清除帖子详情缓存
        redisUtil.delete("post:detail:" + request.getPostId());

        log.info("评论创建成功: userId={}, postId={}, commentId={}", userId, request.getPostId(), comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException("评论不存在");
        }

        // 验证权限：评论作者或管理员可以删除
        User user = userMapper.selectById(userId);
        if (!comment.getAuthorId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION);
        }

        // 软删除评论
        comment.setStatus(0);
        commentMapper.updateById(comment);

        // 更新帖子的评论数
        postMapper.updateCommentCount(comment.getPostId(), -1);

        // 清除缓存
        redisUtil.delete("post:detail:" + comment.getPostId());

        log.info("评论删除成功: userId={}, commentId={}", userId, commentId);
    }

    @Override
    public List<CommentResponse> getPostComments(Long postId, Long currentUserId) {
        List<Comment> comments = commentMapper.selectByPostId(postId);

        return comments.stream()
                .map(comment -> convertToResponse(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public Long getCommentCount(Long postId) {
        return commentMapper.countByPostId(postId);
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
        notification.setContent(content);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
    }

    /**
     * 转换为响应对象
     */
    private CommentResponse convertToResponse(Comment comment, Long currentUserId) {
        User author = userMapper.selectById(comment.getAuthorId());

        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = likeService.getLikeStatus(currentUserId, "comment", comment.getId());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(author != null ? author.getUsername() : "未知用户")
                .authorAvatar(author != null ? author.getAvatar() : null)
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .createTime(comment.getCreateTime())
                .build();
    }
}