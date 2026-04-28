package com.forum.service;

import com.forum.dto.request.CommentCreateRequest;
import com.forum.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {

    /**
     * 发表评论
     */
    Long createComment(Long userId, CommentCreateRequest request);

    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 获取帖子的评论列表
     */
    List<CommentResponse> getPostComments(Long postId, Long currentUserId);

    /**
     * 获取评论数量
     */
    Long getCommentCount(Long postId);
}