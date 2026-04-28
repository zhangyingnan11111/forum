package com.forum.service;

public interface LikeService {

    /**
     * 点赞
     */
    void like(Long userId, String targetType, Long targetId);

    /**
     * 取消点赞
     */
    void unlike(Long userId, String targetType, Long targetId);

    /**
     * 获取点赞状态
     */
    boolean getLikeStatus(Long userId, String targetType, Long targetId);

    /**
     * 获取点赞数量
     */
    Integer getLikeCount(String targetType, Long targetId);
}