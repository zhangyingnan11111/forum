package com.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.dto.request.PostCreateRequest;
import com.forum.dto.request.PostSearchRequest;
import com.forum.dto.request.PostUpdateRequest;
import com.forum.dto.response.CategoryResponse;
import com.forum.dto.response.PostDetailResponse;
import com.forum.dto.response.PostResponse;
import com.forum.entity.Post;

import java.util.List;

public interface PostService {

    /**
     * 创建帖子
     */
    Long createPost(Long userId, PostCreateRequest request);

    /**
     * 更新帖子
     */
    void updatePost(Long userId, Long postId, PostUpdateRequest request);

    /**
     * 删除帖子（软删除）
     */
    void deletePost(Long userId, Long postId);

    /**
     * 获取帖子详情
     */
    PostDetailResponse getPostDetail(Long postId, Long currentUserId);

    /**
     * 分页查询帖子列表
     */
    Page<PostResponse> getPostList(PostSearchRequest request);

    /**
     * 获取所有分类
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 置顶帖子（管理员/版主）
     */
    void topPost(Long postId, boolean isTop);

    /**
     * 加精帖子（管理员/版主）
     */
    void essencePost(Long postId, boolean isEssence);

    /**
     * 获取帖子实体
     */
    Post getPostById(Long postId);
}